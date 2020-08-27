package algorithms.honors;

import java.util.ArrayList;
import model.Network.Vertex;

/**
 * A Face is a part of the plane that has a number of vertices on its border.
 */
public class Face {

	/**
	 * Counter of next ID to give out.
	 */
	private static int FACE_ID_COUNTER = 1;
	
	/**
	 * ID of this face. Guaranteed to be unique as long as there are at most
	 * {@link Integer#MAX_VALUE} - {@link Integer#MIN_VALUE} faces around.
	 */
	public final int ID;
	/**
	 * List of vertices on the border of this Face.
	 */
	public ArrayList<Vertex> vertices;
	
	/**
	 * Create a Face without vertices on its border.
	 */
	public Face() {
		this(new ArrayList<Vertex>());
	}
	
	/**
	 * Create a face with the given vertices on its border.
	 * 
	 * @param vertices Vertices on the border of the face to be created.
	 */
	public Face(Vertex... vertices) {
		this.vertices = new ArrayList<>();
		for (Vertex v : vertices) {
			this.vertices.add(v);
		}
		this.ID = FACE_ID_COUNTER++;
	}
	
	/**
	 * Create a face with the given vertices on its border.
	 * 
	 * @param vertices Vertices on the border of the face to be created.
	 */
	public Face(ArrayList<Vertex> vertices) {
		if (vertices == null) {
			this.vertices = new ArrayList<>();
		} else {
			this.vertices = vertices;
		}
		this.ID = FACE_ID_COUNTER++;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((vertices == null) ? 0 : vertices.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Face))
			return false;
		Face other = (Face) obj;
		if (vertices == null) {
			if (other.vertices != null)
				return false;
		} else if (!vertices.equals(other.vertices))
			return false;
		return true;
	}
	
	/**
	 * Returns if this face has at least {@code num} points in common
	 * with the given face.
	 * 
	 * @param other Face to compare with.
	 * @param num Number of points to have in common.
	 * @return If the given face shares {@code num} points with this one.
	 */
	public boolean sharesPointsWith(Face other, int num) {
		if (num == 0)  return true;
		if (num < 0 || num > this.vertices.size()) {
			return false;
		}
		
		int numShared = 0;
		
		for (Vertex v : this.vertices) {
			if (other.vertices.contains(v)) {
				numShared++;
				if (numShared == num) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return vertices.toString();
	}
}
