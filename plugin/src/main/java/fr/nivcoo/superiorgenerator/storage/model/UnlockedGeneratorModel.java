package fr.nivcoo.superiorgenerator.storage.model;

import fr.nivcoo.utilsz.core.database.ColumnType;
import fr.nivcoo.utilsz.core.database.DatabaseModel;
import fr.nivcoo.utilsz.core.database.DatabaseRow;
import fr.nivcoo.utilsz.core.database.ModelSchema;

import java.util.UUID;

public record UnlockedGeneratorModel(UUID islandUuid, String generatorId) {

    public static final DatabaseModel<UnlockedGeneratorModel> MODEL = new DatabaseModel<>() {
        @Override
        public ModelSchema<UnlockedGeneratorModel> schema() {
            return ModelSchema.<UnlockedGeneratorModel>of("unlocked_generator")
                    .column("island_uuid", ColumnType.UUID, UnlockedGeneratorModel::islandUuid)
                    .column("generator_id", ColumnType.STRING, 128, UnlockedGeneratorModel::generatorId)
                    .constraint("PRIMARY KEY (island_uuid, generator_id)")
                    .index("idx_unlocked_generator_island", "island_uuid");
        }

        @Override
        public UnlockedGeneratorModel from(DatabaseRow row) {
            return new UnlockedGeneratorModel(
                    row.getUuid("island_uuid"),
                    row.getString("generator_id")
            );
        }
    };
}
