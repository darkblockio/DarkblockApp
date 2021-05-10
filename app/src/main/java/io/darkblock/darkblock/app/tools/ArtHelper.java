package io.darkblock.darkblock.app.tools;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.darkblock.darkblock.app.App;
import io.darkblock.darkblock.app.Artwork;
import io.darkblock.darkblock.app.Util;
import io.darkblock.darkblock.transaction.Transaction;

// Abstraction layer to access artwork
public class ArtHelper {

    private static final List<Artwork> ARTWORK_LIST = new ArrayList<>();
    private static boolean needUpdate = false;

    // Get all art
    public static List<Artwork> getArtworkList() {
        return ARTWORK_LIST;
    }

    // Get the number of pieces of art
    public static int getArtListSize() {
        return ARTWORK_LIST.size();
    }

    // Get an artwork from it's numerical id
    public static Artwork getArtByNum(int pos) {
        return ARTWORK_LIST.get(pos);
    }

    // String to use to query graphQL and get all transactions
    private static final String ENDPOINT = "https://arweave.net/graphql";

    // This query retrieves *every* Darkblock object
    private static final String ALL_DARKBLOCK_QUERY = "query {\n" +
            "  transactions(first: 500, tags: { name: \"Asset-Type\", values: [\"NFT\"] }) {\n" +
            "    edges {\n" +
            "      node {\n" +
            "        id\n" +
            "        owner {address}\n" +
            "        tags {\n" +
            "          name\n" +
            "          value\n" +
            "}}}}}";
    // This query gets the
    private static final String MOST_RECENT_OWNER_QUERY = "query {\n" +
            "  transactions(first: 500, tags: [{ name: \"App-Name\", values: [\"SmartWeaveAction\"] }, \n" +
            "      {name: \"Contract\", values: %s } ]\n" +
            "  ) {\n" +
            "    edges {\n" +
            "      node {\n" +
            "        id\n" +
            "        owner {address}\n" +
            "        tags {\n" +
            "          name\n" +
            "          value\n" +
            "}}}}}";

    /**
     * Grab all artwork from the app's wallet ID
     */
    public static int fetchArtwork() {

        // Check for wallet id
        String wallet = App.getSession().getWalletId();
        if (wallet == null) {
            System.err.println("No wallet to retrieve from!");
            return -1;
        }

        // Get every darkblock (or at least a bunch of them)
        Transaction[] transactions = Transaction.queryTransactions(ALL_DARKBLOCK_QUERY);
        if (transactions != null) {
            System.out.println( "found " + transactions.length + " nfts" );
            // Create list of IDs
            HashMap<String, Transaction> map = new HashMap<>();
            for (Transaction t : transactions) {
                //System.out.println( "nft found: " + t.getId() );
                map.put(t.getId(), t);
            }

            // Ok, time for the second transaction....
            StringBuilder formattedIdArray = new StringBuilder("[");
            for (String id : map.keySet()) {
                formattedIdArray.append("\"");
                formattedIdArray.append(id);
                formattedIdArray.append("\",");
            }
            formattedIdArray.append("]");

            // Get transaction verification
            Transaction[] ownershipTransactions = Transaction.queryTransactions(String.format(MOST_RECENT_OWNER_QUERY, formattedIdArray));
            if (ownershipTransactions != null) {
                System.err.println( ownershipTransactions.length + " transactions found" );
                // Ok, so.
                // We loop over every ownership transaction and check for a matching 'Contract' tag
                for (Transaction t : ownershipTransactions) {
                    // Get contract ID
                    String contractId = t.getTagOr("Contract", "");
                    if (map.containsKey(contractId)) {
                        // Ok, we found a match
                        // First, give this transaction a custom 'Override Owner' tag
                        // We need to get this from the ownership transaction
                        String newOwner = "";
                        try {
                            JSONObject obj = new JSONObject(t.getTag("Input"));
                            newOwner = obj.getString("target");
                            if( newOwner != null ) {
                                //System.err.println( contractId + " is now owned by " + newOwner );
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        if (newOwner.length() > 0) {
                            Transaction transaction = map.get(contractId);
                            transaction.putTag("Override Owner ID", newOwner);
                            // Ok, finally, remove the id from the map so that we won't do this again
                            map.remove(contractId);
                        }
                    }
                }
            } else {
                // Don't continue if we can't get the ownership info
                return -1;
            }

            // Ok! At this point we should have a list of transactions, with their owners attached
            // Let's create the art array
            List<Artwork> newArtList = new ArrayList<>();
            for (Transaction t : transactions) {
                // Ignore non-original transactions
                String assetType = t.getTagOr("Asset-Type", "");
                if (assetType.equals("NFT")) {

                    // Get owner id
                    JSONObject state = null;
                    try {
                        state = new JSONObject(t.getTag("Init-State"));
                    }catch( Exception e ){
                        //e.printStackTrace();
                        System.out.println("Error getting initial state");
                    }
                    String ownerId = t.getTag("Override Owner ID");
                    if (ownerId == null && state != null) {
                        // Get owner ID
                        try {
                            ownerId = state.getJSONObject("balances").keys().next();
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.out.println("Cannot find 'balances'!!!");
                        }
                    }
                    // Ok, so do we own this?
                    if (ownerId != null && ownerId.equals(wallet)) {

                        //System.out.println(t);

                        // Hell yeah we do
                        // We found one!
                        Artwork art = new Artwork();
                        // Assign identification info
                        art.setArtId(UUID.fromString(t.getTag("Artid")));
                        art.setTransactionId(t.getId());
                        art.setEncrypted(t.getTag("Content-Type").equals("Encrypted"));
                        art.setDarkblockId(t.getTag("Darkblock"));
                        art.setCreator(t.getOwner());

                        try {
                            art.setTitle(state.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        art.setAuthor(t.getTag("Creator"));


                        // Try and get additional metadata
                        /*if (t.hasTag("metadata")) {
                            String metaUrl = App.getSession().getArweaveResourceURL(t.getTag("metadata"));
                            try {
                                // Perform HTTP request
                                HttpRequest metaRequest = HttpRequest.get(metaUrl);
                                JSONObject metadata = Util.doJsonRequest(metaRequest);
                                if (metadata != null) {
                                    // Assign values
                                    art.setTitle(metadata.getString("name"));
                                    art.setDescription(metadata.getString("description"));
                                    art.setAuthor(metadata.getString("artist"));

                                    String creationDate = metadata.getString("created");
                                    String[] dateParts = creationDate.split("-");
                                    if (dateParts.length == 3) {
                                        int year = Integer.parseInt(dateParts[0]);
                                        int month = Integer.parseInt(dateParts[1]);
                                        int day = Integer.parseInt(dateParts[2]);

                                        art.setCreationDate(LocalDate.of(year, month, day));
                                    }
                                } else {
                                    System.err.println("Something went wrong getting meta json!");
                                }
                            } catch (JSONException e) {
                                // Something went wrong
                                e.printStackTrace();
                            }
                        }*/

                        // Figure out the thumbnail id
                        String thumb = t.getId();//.getTag("thumbnail");

                        // Get the original owner's wallet and description from init-state
                        String thumbWallet = ownerId;
                        try {
                            //JSONObject state = new JSONObject(t.getTag("Init-State"));
                            art.setDescription(state.getString("description"));
                            thumbWallet = state.getJSONObject("balances").keys().next();
                        }catch (JSONException ex){
                            ex.printStackTrace();
                            thumbWallet = ownerId;
                        }
                        art.setThumbnailImagUrl("https://"+thumbWallet+".arweave.net/"+thumb);

                        // add it
                        newArtList.add(art);
                    }else if (ownerId != null) {
                        // We don't own this :grimacing:
                        // Check if we have a decoding key for it
                        String artId = t.getTag("artid");
                        if (artId != null) {

                            UUID id = UUID.fromString(artId);
                            String key = App.getKeyFromUUID(id);
                            // do we have a key?
                            if (key != null) {
                                // Get rid of that
                                System.err.println( "we do not own " + artId + " so let's transfer it");
                                TransferManager.disposeArtKey(wallet,ownerId,id,key);
                            }
                        }
                    }
                }
            }
            // Sort
            /*newArtList.sort(new Comparator<Artwork>() {
                @Override
                public int compare(Artwork o1, Artwork o2) {
                    return o2.getCreationDate().compareTo(o1.getCreationDate());
                }
            });*/
            // Set ids
            for (int i=0;i<newArtList.size();i++) {
                newArtList.get(i).setNum(i);
            }

            needUpdate = ARTWORK_LIST.hashCode() != newArtList.hashCode();

            // Copy over
            ARTWORK_LIST.clear();
            ARTWORK_LIST.addAll(newArtList);

            System.err.println("Done loading! Found " + ARTWORK_LIST.size() + " pieces of art. " + (needsUpdate() ? "Requesting ui reload..." : "No change found"));

            return ARTWORK_LIST.size();
        }
        return -1;
    }

    /**
     * Do we need to update our UI?
     * @return
     */
    public static boolean needsUpdate() {
        return needUpdate;
    }


}