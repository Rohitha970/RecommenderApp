import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class RecommenderSystemApp {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            DataModel model = new FileDataModel(new File("data.csv"));

            System.out.println(BOLD + CYAN + "\n🔍 Welcome to the Real-Time Product Recommender System" + RESET);
            System.out.print(YELLOW + "\n📊 Available user IDs: " + RESET);
            LongPrimitiveIterator users = model.getUserIDs();
            while (users.hasNext()) {
                System.out.print(users.nextLong() + " ");
            }
            System.out.println();

            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
            Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

            while (true) {
                System.out.print(GREEN + "\nEnter your User ID to get recommendations (or 0 to exit): " + RESET);
                long userId;
                try {
                    userId = Long.parseLong(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println(RED + "❌ Invalid input. Please enter a numeric User ID." + RESET);
                    continue;
                }

                if (userId == 0) {
                    System.out.println(YELLOW + "\n👋 Thank you for using the Recommender System. Goodbye!" + RESET);
                    break;
                }

                try {
                    List<RecommendedItem> recommendations = recommender.recommend(userId, 5);
                    if (recommendations.isEmpty()) {
                        System.out.println(RED + "⚠️ No recommendations found for user ID: " + userId + RESET);
                    } else {
                        System.out.println(GREEN + BOLD + "\n✅ Top Recommendations for User " + userId + ":" + RESET);
                        for (RecommendedItem item : recommendations) {
                            System.out.printf(CYAN + "📦 Item %-5d → Score: %.2f\n" + RESET, item.getItemID(), item.getValue());
                        }
                    }
                } catch (TasteException e) {
                    System.out.println(RED + "❌ Error retrieving recommendations: " + e.getMessage() + RESET);
                }
            }
        } catch (IOException | TasteException e) {
            System.out.println(RED + "❌ Failed to initialize the recommender system: " + e.getMessage() + RESET);
        } finally {
            scanner.close();
        }
    }
}
