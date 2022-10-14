/*-
 * #%L
 * Fiji viewer for MoBIE projects
 * %%
 * Copyright (C) 2018 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.embl.mobie.viewer.create;

import org.embl.mobie.io.util.IOHelper;
import org.embl.mobie.viewer.serialize.Dataset;
import org.embl.mobie.viewer.serialize.Project;
import org.embl.mobie.viewer.serialize.DatasetJsonParser;
import org.embl.mobie.viewer.serialize.ProjectJsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetsCreatorTest {

    private ProjectCreator projectCreator;
    private DatasetsCreator datasetsCreator;

    @BeforeEach
    void setUp( @TempDir Path tempDir ) throws IOException {
        projectCreator = new ProjectCreator( tempDir.toFile() );
        datasetsCreator = projectCreator.getDatasetsCreator();
    }

    @Test
    void addDataset() throws IOException {
        String datasetName = "test";
        datasetsCreator.addDataset(datasetName, false);

        Project project = new ProjectJsonParser().parseProject( projectCreator.getProjectJson().getAbsolutePath() );
        assertEquals(project.getDatasets().size(), 1);
        assertEquals(project.getDatasets().get(0), datasetName);
        assertTrue(new File(projectCreator.getProjectLocation(), datasetName).exists());
    }

    @Test
    void renameDataset() throws IOException {
        String oldDatasetName = "test";
        String newDatasetName = "newName";
        datasetsCreator.addDataset(oldDatasetName, false);
        datasetsCreator.renameDataset(oldDatasetName, newDatasetName);

        Project project = new ProjectJsonParser().parseProject( projectCreator.getProjectJson().getAbsolutePath() );
        assertEquals(project.getDatasets().size(), 1);
        assertEquals(project.getDatasets().get(0), newDatasetName);
        assertTrue(new File(projectCreator.getProjectLocation(), newDatasetName).exists());
        assertFalse(new File(projectCreator.getProjectLocation(), oldDatasetName).exists());
    }

    @Test
    void makeDefaultDataset() throws IOException {
        String dataset1Name = "dataset1";
        String dataset2Name = "dataset2";
        datasetsCreator.addDataset(dataset1Name, false);
        datasetsCreator.addDataset(dataset2Name, false);

        Project project;
        project = new ProjectJsonParser().parseProject( projectCreator.getProjectJson().getAbsolutePath() );
        assertEquals( project.getDefaultDataset(), dataset1Name );

        datasetsCreator.makeDefaultDataset( dataset2Name );

        project = new ProjectJsonParser().parseProject( projectCreator.getProjectJson().getAbsolutePath() );
        assertEquals( project.getDefaultDataset(), dataset2Name );
    }

    @Test
    void makeDataset2D() throws IOException {
        String datasetName = "test";
        String datasetJsonPath = IOHelper.combinePath( projectCreator.getProjectLocation().getAbsolutePath(),
                datasetName, "dataset.json" );

        Dataset dataset;
        datasetsCreator.addDataset(datasetName, false);
        dataset = new DatasetJsonParser().parseDataset( datasetJsonPath );
        assertFalse( dataset.is2D );

        datasetsCreator.makeDataset2D(datasetName, true);
        dataset = new DatasetJsonParser().parseDataset( datasetJsonPath );
        assertTrue( dataset.is2D );
    }
}