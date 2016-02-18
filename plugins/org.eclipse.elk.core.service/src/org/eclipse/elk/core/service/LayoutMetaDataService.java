/*******************************************************************************
 * Copyright (c) 2008, 2015 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.core.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.elk.core.data.ILayoutMetaDataProvider;
import org.eclipse.elk.core.data.LayoutAlgorithmData;
import org.eclipse.elk.core.data.LayoutCategoryData;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.options.LayoutOptions;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.collect.Maps;

/**
 * Singleton class for access to the KIML layout meta data. This class is used globally to retrieve
 * meta data for automatic layout through KIML, which is given through the {@code layoutProviders}
 * extension point.
 * 
 * @kieler.design 2011-03-14 reviewed by cmot, cds
 * @kieler.rating yellow 2012-10-09 review KI-25 by chsch, bdu
 * @author msp
 */
public class LayoutMetaDataService {
    
    /** identifier of the extension point for layout providers. */
    protected static final String EXTP_ID_LAYOUT_PROVIDERS = "org.eclipse.elk.core.service.layoutProviders";
    /** name of the 'provider' element in the 'layout providers' extension point. */
    protected static final String ELEMENT_PROVIDER = "provider";
    /** name of the 'class' attribute in the extension points. */
    protected static final String ATTRIBUTE_CLASS = "class";

    /** the layout data service instance, which is created lazily. */
    private static LayoutMetaDataService instance;

    /**
     * Returns the singleton instance of the layout data service.
     * 
     * @return the singleton instance
     */
    public static synchronized LayoutMetaDataService getInstance() {
        if (instance == null) {
            instance = new LayoutMetaDataService();
        }
        return instance;
    }
    
    /**
     * Unload any created instance in order to feed the garbage collector.
     */
    protected static synchronized void unload() {
        if (instance != null) {
            for (LayoutAlgorithmData algoData : instance.getAlgorithmData()) {
                algoData.getInstancePool().clear();
            }
            instance = null;
        }
    }
    

    /** mapping of layout provider identifiers to their data instances. */
    private final Map<String, LayoutAlgorithmData> layoutAlgorithmMap = Maps.newLinkedHashMap();
    /** mapping of layout option identifiers to their data instances. */
    private final Map<String, LayoutOptionData> layoutOptionMap = Maps.newLinkedHashMap();
    /** mapping of layout category identifiers to their data instances. */
    private final Map<String, LayoutCategoryData> layoutCategoryMap = Maps.newLinkedHashMap();
    
    /** additional map of layout algorithm suffixes to data instances. */
    private final Map<String, LayoutAlgorithmData> algorithmSuffixMap = Maps.newHashMap();
    /** additional map of layout option suffixes to data instances. */
    private final Map<String, LayoutOptionData> optionSuffixMap = Maps.newHashMap();
    
    /**
     * Load all registered extensions for the layout providers extension point.
     */
    public LayoutMetaDataService() {
        loadLayoutProviderExtensions();
    }

    /**
     * Returns the layout algorithm data associated with the given identifier.
     * 
     * @param id
     *            layout algorithm identifier
     * @return the corresponding layout algorithm data, or {@code null} if there is no algorithm
     *         with the given identifier
     */
    public final LayoutAlgorithmData getAlgorithmData(final String id) {
        return layoutAlgorithmMap.get(id);
    }

    /**
     * Returns a data collection for all registered layout algorithms. The collection is
     * unmodifiable.
     * 
     * @return collection of registered layout algorithms
     */
    public final Collection<LayoutAlgorithmData> getAlgorithmData() {
        return Collections.unmodifiableCollection(layoutAlgorithmMap.values());
    }
    
    /**
     * Returns a layout algorithm data that has the given suffix in its identifier.
     * 
     * @param suffix
     *            a layout algorithm identifier suffix
     * @return the first layout algorithm data that has the given suffix, or {@code null} if
     *          no algorithm has that suffix
     */
    public final LayoutAlgorithmData getAlgorithmDataBySuffix(final String suffix) {
        LayoutAlgorithmData data = layoutAlgorithmMap.get(suffix);
        if (data == null) {
            data = algorithmSuffixMap.get(suffix);
            if (data == null) {
                for (LayoutAlgorithmData d : layoutAlgorithmMap.values()) {
                    String id = d.getId();
                    if (id.endsWith(suffix) && (suffix.length() == id.length()
                            || id.charAt(id.length() - suffix.length() - 1) == '.')) {
                        algorithmSuffixMap.put(suffix, d);
                        return d;
                    }
                }
            }
        }
        return data;
    }

    /**
     * Returns the layout option data associated with the given identifier.
     * 
     * @param id
     *            layout option identifier
     * @return the corresponding layout option data, or {@code null} if there is no option with the
     *         given identifier
     */
    public final LayoutOptionData getOptionData(final String id) {
        return layoutOptionMap.get(id);
    }

    /**
     * Returns a data collection for all registered layout options. The collection is unmodifiable.
     * 
     * @return collection of registered layout options
     */
    public final Collection<LayoutOptionData> getOptionData() {
        return Collections.unmodifiableCollection(layoutOptionMap.values());
    }
    
    /**
     * Returns a layout option data that has the given suffix in its identifier.
     * 
     * @param suffix
     *            a layout option identifier suffix
     * @return the first layout option data that has the given suffix, or {@code null} if
     *          no option has that suffix
     */
    public final LayoutOptionData getOptionDataBySuffix(final String suffix) {
        LayoutOptionData data = layoutOptionMap.get(suffix);
        if (data == null) {
            data = optionSuffixMap.get(suffix);
            if (data == null) {
                for (LayoutOptionData d : layoutOptionMap.values()) {
                    String id = d.getId();
                    if (id.endsWith(suffix) && (suffix.length() == id.length()
                            || id.charAt(id.length() - suffix.length() - 1) == '.')) {
                        optionSuffixMap.put(suffix, d);
                        return d;
                    }
                }
            }
        }
        return data;
    }

    /**
     * Returns a list of layout options that are suitable for the given layout algorithm and layout
     * option target. The layout algorithm must know the layout options and at the target must be
     * active for each option.
     * 
     * @param algorithmData
     *            layout algorithm data
     * @param targetType
     *            type of layout option target
     * @return list of suitable layout options
     */
    public final List<LayoutOptionData> getOptionData(final LayoutAlgorithmData algorithmData,
            final LayoutOptionData.Target targetType) {
        List<LayoutOptionData> optionDataList = new LinkedList<LayoutOptionData>();
        for (LayoutOptionData optionData : layoutOptionMap.values()) {
            if (algorithmData.knowsOption(optionData)
                    || LayoutOptions.ALGORITHM.equals(optionData)) {
                if (optionData.getTargets().contains(targetType)) {
                    optionDataList.add(optionData);
                }
            }
        }
        return optionDataList;
    }

    /**
     * Returns the data instance of the layout category with given identifier.
     * 
     * @param id
     *            identifier of the category
     * @return layout category data instance with given identifier, or {@code null} if the layout category
     *         is not registered
     */
    public final LayoutCategoryData getCategoryData(final String id) {
        return layoutCategoryMap.get(id);
    }

    /**
     * Returns a list of layout category identifiers and names. The first string in each entry is the
     * identifier, and the second string is the name.
     * 
     * @return a list of all layout categories
     */
    public final Collection<LayoutCategoryData> getCategoryData() {
        return Collections.unmodifiableCollection(layoutCategoryMap.values());
    }

    /**
     * Loads all layout metadata provider extensions from the extension point.
     */
    private void loadLayoutProviderExtensions() {
        IConfigurationElement[] extensions = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTP_ID_LAYOUT_PROVIDERS);
        Registry registry = new Registry();
        
        for (IConfigurationElement element : extensions) {
            try {
                if (ELEMENT_PROVIDER.equals(element.getName())) {
                    ILayoutMetaDataProvider provider = (ILayoutMetaDataProvider)
                            element.createExecutableExtension(ATTRIBUTE_CLASS);
                    if (provider != null) {
                        provider.apply(registry);
                    }
                }
            } catch (CoreException exception) {
                StatusManager.getManager().handle(exception, ElkServicePlugin.PLUGIN_ID);
            }
        }
        
        registry.applyDependencies();
    }
    
    /**
     * Class used to register layout services.
     */
    protected class Registry implements ILayoutMetaDataProvider.Registry {
        
        /**
         * Data class for storing dependencies before they are actually registered.
         */
        private class Triple {
            private String firstId;
            private String secondId;
            private Object value;
        }
        
        private final List<Triple> optionDependencies = new LinkedList<Triple>();
        private final List<Triple> optionSupport = new LinkedList<Triple>();
        
        @Override
        public void register(final LayoutAlgorithmData algorithmData) {
            layoutAlgorithmMap.put(algorithmData.getId(), algorithmData);
        }

        @Override
        public void register(final LayoutOptionData optionData) {
            layoutOptionMap.put(optionData.getId(), optionData);
            if (optionData.getLegacyIds() != null) {
                for (String legacyId : optionData.getLegacyIds()) {
                    layoutOptionMap.put(legacyId, optionData);
                }
            }
        }

        @Override
        public void register(final LayoutCategoryData categoryData) {
            layoutCategoryMap.put(categoryData.getId(), categoryData);
        }

        @Override
        public void addDependency(final String sourceOption, final String targetOption, final Object requiredValue) {
            Triple dep = new Triple();
            dep.firstId = sourceOption;
            dep.secondId = targetOption;
            dep.value = requiredValue;
            optionDependencies.add(dep);
        }

        @Override
        public void addOptionSupport(final String algorithm, final String option, final Object defaultValue) {
            Triple sup = new Triple();
            sup.firstId = algorithm;
            sup.secondId = option;
            sup.value = defaultValue;
            optionSupport.add(sup);
        }
        
        /**
         * Apply all dependencies that have been delivered by layout meta data providers.
         */
        protected void applyDependencies() {
            for (LayoutAlgorithmData algorithm : layoutAlgorithmMap.values()) {
                String categoryId = algorithm.getCategoryId();
                if (categoryId == null) {
                    categoryId = "";
                }
                LayoutCategoryData category = getCategoryData(categoryId);
                if (category == null && categoryId.isEmpty()) {
                    category = new LayoutCategoryData("", "Other", null);
                    layoutCategoryMap.put("", category);
                }
                if (category != null) {
                    category.getLayouters().add(algorithm);
                }
            }
            for (Triple dep : optionDependencies) {
                LayoutOptionData source = getOptionData(dep.firstId);
                LayoutOptionData target = getOptionData(dep.secondId);
                if (source != null && target != null) {
                    source.getDependencies().add(Pair.of(target, dep.value));
                }
            }
            optionDependencies.clear();
            for (Triple sup : optionSupport) {
                LayoutAlgorithmData algorithm = getAlgorithmData(sup.firstId);
                LayoutOptionData option = getOptionData(sup.secondId);
                if (algorithm != null && option != null) {
                    algorithm.addKnownOption(option, sup.value);
                }
            }
            optionSupport.clear();
        }

    }

}