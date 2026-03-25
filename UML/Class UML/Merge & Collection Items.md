## Inventory & Item Collection Class UML
```mermaid
classDiagram
%% ==========================================
%% 1. CORE LIBGDX & ASHLEY API
%% ==========================================
    class InputProcessor {
<<Interface - LibGDX>>
+touchDown(int screenX, int screenY, int pointer, int button) boolean
}
class EntitySystem {
<<Class - Ashley>>
+update(float deltaTime)
}
class Engine {
<<Class - Ashley>>
+removeEntity(Entity entity)
}
class ComponentMapper~T~ {
<<Class - Ashley>>
+get(Entity e) T
}

%% ==========================================
%% 2. NHÓM COMPONENTS (DỮ LIỆU VẬT THỂ)
%% ==========================================
class TransformComponent {
<<Component>>
+Vector2 position
}
class BoundsComponent {
<<Component>>
+Rectangle hitbox
}
class ItemInfoComponent {
<<Component>>
+String itemID
+String inspectText
+String pickupSoundID
}
class CollectibleComponent {
<<Component - Marker>>
}
class RSModifierComponent {
<<Component>>
+int rsChangeValue
}

%% ==========================================
%% 3. NHÓM MODELS (SINGLETON)
%% ==========================================
class InventoryManager {
<<Singleton / Model>>
-ArrayList~String~ items
+addItem(String itemID)
    }
class GameStateManager {
<<Singleton / Model>>
-HashMap~String, Boolean~ globalFlags
+setFlag(String flagID, boolean value)
}
class RSManager {
<<Singleton / Model>>
-int currentRS
+addRS(int value)
}

%% ==========================================
%% 4. NHÓM VIEW & UTILITY (PHẢN HỒI)
%% ==========================================
class AudioManager {
<<Singleton / Utility>>
+playSound(String soundID)
}
class GameUI {
<<View - Scene2D>>
+showNotification(String message)
+updateInventoryBar(ArrayList items)
+showDialogue(String text)
}

%% ==========================================
%% 5. BỘ NÃO ĐIỀU KHIỂN (CONTROLLER / SYSTEM)
%% ==========================================
class PlayerInteractionSystem {
<<System / Controller>>
-Engine engine
-OrthographicCamera camera

%% Mappers để lấy Component siêu nhanh
-ComponentMapper~BoundsComponent~ bm
-ComponentMapper~ItemInfoComponent~ im
-ComponentMapper~CollectibleComponent~ cm
-ComponentMapper~RSModifierComponent~ rm

-AudioManager audioManager
-GameUI gameUI

+touchDown(int screenX, int screenY, int pointer, int button) boolean
-processClick(float worldX, float worldY)
-pickupItem(Entity entity)
-inspectItem(Entity entity)
}

%% --- MỐI QUAN HỆ LẮNG NGHE & KẾ THỪA ---
PlayerInteractionSystem ..|> InputProcessor : "Lắng nghe click chuột"
PlayerInteractionSystem --|> EntitySystem : "Chạy ngầm liên tục"

%% --- MỐI QUAN HỆ GỌI HÀM ---
PlayerInteractionSystem --> Engine : "Gọi xóa Entity"
PlayerInteractionSystem --> ComponentMapper : "Quét Component"
PlayerInteractionSystem --> BoundsComponent : "Check hitbox.contains(x,y)"

PlayerInteractionSystem --> InventoryManager : "Lưu vào túi đồ"
PlayerInteractionSystem --> GameStateManager : "Cập nhật Flag"
PlayerInteractionSystem --> RSManager : "Cộng/Trừ RS"

PlayerInteractionSystem --> AudioManager : "Phát tiếng động"
PlayerInteractionSystem --> GameUI : "Cập nhật giao diện"
```
---
## Item Merging Logic Class UML
```mermaid
classDiagram
    %% ==========================================
    %% 1. NHÓM ĐIỀU KHIỂN UI (UI CONTROLLER)
    %% ==========================================
    class InventoryUIController {
        <<Controller>>
        -String selectedItemID
        -GameUI gameUI
        -CraftingManager craftingManager
        -InventoryManager inventoryManager
        -RSManager rsManager
        -GameStateManager gameStateManager
        
        +onItemClicked(String itemID)
        -processMerge(String itemA, String itemB)
        -resetSelection()
    }

    %% ==========================================
    %% 2. NHÓM QUẢN LÝ CÔNG THỨC & DỮ LIỆU (MODEL)
    %% ==========================================
    class CraftingManager {
        <<Singleton / Logic>>
        -HashMap~String, String~ recipes
        +getMergeResult(String itemA, String itemB) String
    }

    class ItemDatabase {
        <<Singleton / Data>>
        -HashMap~String, ItemData~ database
        +getItemData(String itemID) ItemData
    }

    class ItemData {
        <<Data Structure>>
        +String itemID
        +String itemName
        +int rsChangeValue
        +boolean isCursed
    }

    %% ==========================================
    %% 3. NHÓM KHO ĐỒ & THỰC TẠI (SINGLETON MODELS)
    %% ==========================================
    class InventoryManager {
        <<Singleton / Model>>
        -ArrayList~String~ items
        +addItem(String itemID)
        +removeItem(String itemID)
    }

    class RSManager {
        <<Singleton / Model>>
        -int currentRS
        +addRS(int value)
    }

    class GameStateManager {
        <<Singleton / Model>>
        +setFlag(String flagID, boolean value)
    }

    %% ==========================================
    %% 4. NHÓM GIAO DIỆN (VIEW)
    %% ==========================================
    class GameUI {
        <<View - Scene2D>>
        +showNotification(String message)
        +updateInventoryBar(ArrayList items)
        +highlightSlot(String itemID)
        +removeHighlight(String itemID)
    }

    %% --- MỐI QUAN HỆ TƯƠNG TÁC CHÍNH ---
    InventoryUIController --> GameUI : "1. Ra lệnh Highlight/Báo lỗi"
    InventoryUIController --> CraftingManager : "2. Hỏi kết quả A + B"
    CraftingManager --> ItemDatabase : "3. Tra cứu thông tin Item C"
    ItemDatabase ..> ItemData : "Chứa dữ liệu tĩnh"

    InventoryUIController --> InventoryManager : "4. Xóa A, B & Thêm C"
    InventoryUIController --> RSManager : "5. Cập nhật RS (nếu C có rsChangeValue)"
    InventoryUIController --> GameStateManager : "6. Cập nhật Flag sự kiện"
```