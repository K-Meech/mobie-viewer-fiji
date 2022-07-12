package mobie3.viewer.table;

import net.imglib2.util.Pair;
import tech.tablesaw.api.Table;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TableSawSegmentsTableModel implements SegmentsTableModel< TableSawSegmentAnnotation >
{
	protected final String columnsPath;
	protected Collection< String > columnPaths;
	protected List< String > loadedColumnPaths;

	private HashMap< TableSawSegmentAnnotation, Integer > annotationToRowIndex;
	private HashMap< Integer, TableSawSegmentAnnotation > rowIndexToAnnotation;
	private Table table;

	public TableSawSegmentsTableModel( String columnsPath )
	{
		this.columnsPath = columnsPath;
		annotationToRowIndex = new HashMap<>();
		rowIndexToAnnotation = new HashMap<>();
	}

	@Override
	public List< String > getColumnNames()
	{
		return table.columnNames();
	}

	@Override
	public Class< ? > getColumnClass( String columnName )
	{
		return TableSawColumnTypes.typeToClass.get( table.column( columnName ).type() );
	}

	@Override
	public int getNumRows()
	{
		return table.rowCount();
	}

	@Override
	public int getRowIndex( TableSawSegmentAnnotation annotation )
	{
		return annotationToRowIndex.get( annotation );
	}

	@Override
	public TableSawSegmentAnnotation getRow( int rowIndex )
	{
		if ( ! rowIndexToAnnotation.containsKey( rowIndex ) )
		{
			final TableSawSegmentAnnotation annotation = new TableSawSegmentAnnotation( table.row( rowIndex ) );
			annotationToRowIndex.put( annotation, rowIndex );
			rowIndexToAnnotation.put( rowIndex, annotation );
		}

		return rowIndexToAnnotation.get( rowIndex );
	}

	@Override
	public void loadColumns( String columnsPath )
	{

	}

	@Override
	public void setColumnPaths( Collection< String > columnPaths )
	{
		this.columnPaths = columnPaths;
	}

	@Override
	public Collection< String > getColumnPaths()
	{
		return columnPaths;
	}

	@Override
	public List< String > getLoadedColumnPaths()
	{
		return loadedColumnPaths;
	}

	@Override
	public Pair< Double, Double > getMinMax( String columnName )
	{
		return null;
	}
}