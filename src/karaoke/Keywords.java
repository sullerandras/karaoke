package karaoke;

public class Keywords {
  private String[] keywords;

  public Keywords(String searchString) {
    this.keywords = searchString.strip().toLowerCase().split("\\s+");
  }

  public boolean matches(String text) {
    String lowerText = text.toLowerCase();
    for (String keyword : keywords) {
      if (!lowerText.contains(keyword)) {
        return false;
      }
    }
    return true;
  }
}
