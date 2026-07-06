package fr.nivcoo.superiorgenerator.storage.model;

import fr.nivcoo.utilsz.core.database.ColumnType;
import fr.nivcoo.utilsz.core.database.DatabaseModel;
import fr.nivcoo.utilsz.core.database.DatabaseRow;
import fr.nivcoo.utilsz.core.database.ModelSchema;

import java.util.UUID;

public record ActiveGeneratorModel(UUID islandUuid, String generatorId) {

    public static final DatabaseModel<ActiveGeneratorModel> MODEL = new DatabaseModel<>() {
        @Override
        public ModelSchema<ActiveGeneratorModel> schema() {
            return ModelSchema.<ActiveGeneratorModel>of("active_generator")
                    .column("island_uuid", ColumnType.UUID, "PRIMARY KEY", ActiveGeneratorModel::islandUuid)
                    .column("generator_id", ColumnType.TEXT, ActiveGeneratorModel::generatorId);
        }

        @Override
        public ActiveGeneratorModel from(DatabaseRow row) {
            return new ActiveGeneratorModel(
                    row.getUuid("island_uuid"),
                    row.getString("generator_id")
            );
        }
    };
}
