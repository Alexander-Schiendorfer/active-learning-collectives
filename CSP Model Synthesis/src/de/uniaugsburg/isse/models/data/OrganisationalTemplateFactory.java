package de.uniaugsburg.isse.models.data;

import de.uniaugsburg.isse.models.OrganisationalTemplate;

/**
 * Specifies input sources for organisational templates, could be a naive OPL reader or a sophisticated parser or other
 * resources
 * 
 * @author Alexander Schiendorfer
 * 
 */
public interface OrganisationalTemplateFactory {
	OrganisationalTemplate create();

	OrganisationalTemplate create(String organisationalTemplateFile);
}
