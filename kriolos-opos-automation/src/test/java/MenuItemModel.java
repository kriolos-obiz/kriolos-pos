import java.util.Collections;
import java.util.List;

public record MenuItemModel(
        String id,
        String title,
        String icon, // Representação em texto ou caractere Unicode moderno para simplificar
        List<MenuItemModel> submenus,
        boolean isDropdown
) {
    public static class Builder {
        private final String id;
        private final String title;
        private String icon = "";
        private List<MenuItemModel> submenus = Collections.emptyList();
        private boolean isDropdown = false;

        public Builder(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public Builder withIcon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder withSubmenus(List<MenuItemModel> submenus) {
            this.submenus = submenus;
            this.isDropdown = !submenus.isEmpty();
            return this;
        }

        public MenuItemModel build() {
            return new MenuItemModel(id, title, icon, submenus, isDropdown);
        }
    }
}
