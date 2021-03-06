/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.as.console.client.ui.federation.idp;

import com.google.gwt.user.client.ui.TabPanel;
import com.google.web.bindery.event.shared.EventBus;
import org.picketlink.as.console.client.i18n.PicketLinkUIConstants;
import org.picketlink.as.console.client.i18n.PicketLinkUIMessages;
import org.picketlink.as.console.client.shared.subsys.model.FederationWrapper;
import org.picketlink.as.console.client.shared.subsys.model.IdentityProvider;
import org.picketlink.as.console.client.shared.subsys.model.IdentityProviderHandler;
import org.picketlink.as.console.client.shared.subsys.model.IdentityProviderHandlerParameter;
import org.picketlink.as.console.client.shared.subsys.model.IdentityProviderHandlerWrapper;
import org.picketlink.as.console.client.shared.subsys.model.IdentityProviderWrapper;
import org.picketlink.as.console.client.shared.subsys.model.TrustDomain;
import org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor;
import org.picketlink.as.console.client.ui.federation.FederationPresenter;
import org.picketlink.as.console.client.ui.federation.Wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @since Mar 30, 2012
 */
public class IdentityProviderEditor extends AbstractFederationDetailEditor<IdentityProvider> {

    private final EventBus eventBus;
    private TrustedDomainTabEditor trustedDomainTabEditor;
    private SignatureSupportTabEditor signatureSupportTabEditor;
    private EncryptionSupportTabEditor encryptionSupportTabEditor;
    private IdentityProviderHandlersTabEditor handlersTabEditor;
    private PicketLinkUIConstants uiConstants;
    private PicketLinkUIMessages uiMessages;
    private IdentityProviderWrapper selectedIdentityProvider;

    public IdentityProviderEditor(FederationPresenter presenter,
            final PicketLinkUIConstants uiConstants, final PicketLinkUIMessages uiMessages, EventBus eventBus) {
        super(presenter, new IdentityProviderTable(presenter), IdentityProvider.class);
        this.uiConstants = uiConstants;
        this.uiMessages = uiMessages;
        this.eventBus = eventBus;
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#getEntityName()
     */
    @Override
    public String doGetEntityName() {
        return uiConstants.common_label_identityProvider();
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#doGetDescription()
     */
    @Override
    protected String doGetDescription() {
        return uiConstants.subsys_picketlink_identity_provider_desc();
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#getStackName()
     */
    @Override
    public String doGetTableSectionName() {
        return "Identity Providers";
    }

    @Override
    protected boolean doInsert(IdentityProvider identityProvider) {
        if (identityProvider.isExternal()) {
            identityProvider.setName(getFederation().getName() + "-" + "external-idp");
            identityProvider.setSecurityDomain("no-defined");
        }

        String url = identityProvider.getUrl();

        if (url == null || "".equals(url.trim())) {
            url = "http://localhost:8080/" + identityProvider.getName().replaceAll(".war", "") + "/";
            identityProvider.setUrl(url);
        }

        identityProvider.setStrictPostBinding(true);
        
        getPresenter().getFederationManager().onCreateIdentityProvider(identityProvider);

        return true;
    }

    @Override
    protected String doGetName(IdentityProvider currentSelection) {
        return currentSelection.getName();
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#addTabs(com.google.gwt.user.client.ui.TabPanel)
     */
    @Override
    protected void addDetailsSectionTabs(TabPanel bottomTabs) {
        bottomTabs.add(getSignatureSupportTabEditor().asWidget(), "Signature Policy");
        bottomTabs.add(getEncryptionSupportTabEditor().asWidget(), "Encryption Policy");
        bottomTabs.add(getTrustedDomainTabEditor().asWidget(), "Trusted Domains");
        bottomTabs.add(getHandlerTabEditor().asWidget(), "SAML Handlers");
    }

    private TrustedDomainTabEditor getTrustedDomainTabEditor() {
        if (this.trustedDomainTabEditor == null) {
            this.trustedDomainTabEditor = new TrustedDomainTabEditor(getPresenter(), uiConstants, uiMessages);
        }

        return this.trustedDomainTabEditor;
    }

    private IdentityProviderHandlersTabEditor getHandlerTabEditor() {
        if (this.handlersTabEditor == null) {
            this.handlersTabEditor = new IdentityProviderHandlersTabEditor(getPresenter(), uiConstants, uiMessages);
        }

        return this.handlersTabEditor;
    }

    private SignatureSupportTabEditor getSignatureSupportTabEditor() {
        if (this.signatureSupportTabEditor == null) {
            this.signatureSupportTabEditor = new IdentityProviderSignatureSupportEditor(getPresenter(), uiConstants);
        }

        return this.signatureSupportTabEditor;
    }

    private EncryptionSupportTabEditor getEncryptionSupportTabEditor() {
        if (this.encryptionSupportTabEditor == null) {
            this.encryptionSupportTabEditor = new EncryptionSupportTabEditor(getPresenter());
        }

        return this.encryptionSupportTabEditor;
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#doUpdateSelection(org.picketlink.as.console.client.shared.subsys.model.GenericFederationEntity)
     */
    @Override
    protected void doUpdateSelection(IdentityProvider identityProvider) {
        updateSelectedIdentityProvider(identityProvider);
        getTrustedDomainTabEditor().setIdentityProvider(identityProvider);
        getHandlerTabEditor().setIdentityProvider(this.selectedIdentityProvider);

        if (this.selectedIdentityProvider != null) {
            ArrayList<IdentityProviderHandler> handlersList = new ArrayList<>();

            for (IdentityProviderHandlerWrapper handler : this.selectedIdentityProvider.getHandlers()) {
                handlersList.add(handler.getHandler());
            }

            getHandlerTabEditor().getHandlerTable().getDataProvider().setList(handlersList);
            getHandlerTabEditor().getHandlerTable().getDataTable().selectDefaultEntity();
            getSignatureSupportTabEditor().setEntity(this.selectedIdentityProvider.getIdentityProvider());
        }

        getHandlerTabEditor().getHandlerParameterTable().getDataProvider().setList(new ArrayList<IdentityProviderHandlerParameter>());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.picketlink.as.console.client.ui.federation.idp.AbstractFederationDetailEditor#onDelete(org.picketlink.as.console.
     * client.shared.subsys.model.GenericFederationEntity)
     */
    @Override
    protected void doDelete(IdentityProvider identityProvider) {
        this.getPresenter().getFederationManager().onRemoveIdentityProvider(identityProvider);
        enableAddButton();
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.console.client.ui.federation.AbstractFederationDetailEditor#doUpdate(org.picketlink.as.console.client.shared.subsys.model.GenericFederationEntity, java.util.Map)
     */
    public void doUpdate(IdentityProvider identityProvider, Map<String, Object> changedValues) {
        this.getPresenter().getFederationManager().onUpdateIdentityProvider(identityProvider, changedValues);
    }

    @Override
    public Wizard<IdentityProvider> doCreateWizard() {
        return new NewIdentityProviderWizard(this, getEntityClass(), getPresenter(), "identity-provider", uiConstants, this.eventBus);
    }

    public void updateIdentityProviders(FederationWrapper federation) {
        if (federation != null) {
            List<IdentityProvider> identityProviders = new ArrayList<IdentityProvider>();

            for (IdentityProviderWrapper identityProviderWrapper : federation.getIdentityProviders()) {
                identityProviders.add(identityProviderWrapper.getIdentityProvider());
            }

            setData(federation, identityProviders);

            // disables the add button since we already have a idp configuration
            if (!identityProviders.isEmpty()) {
                disableAddButton();
                enableRemoveButton();
            } else {
                enableAddButton();
                disableRemoveButton();
                this.selectedIdentityProvider = null;
            }

            if (!identityProviders.isEmpty()) {
                IdentityProvider identityProvider = identityProviders.get(0);

                getSignatureSupportTabEditor().setEntity(identityProvider);
                getEncryptionSupportTabEditor().setEntity(identityProvider);

                getBottomTabs().setVisible(!identityProvider.isExternal());
            }

            updateTrustedDomains(federation);
            updateHandlers(this.selectedIdentityProvider);
        }
    }

    private void updateHandlers(IdentityProviderWrapper identityProvider) {
        List<IdentityProviderHandler> handlers = new ArrayList<IdentityProviderHandler>();

        if (identityProvider != null) {
            for (IdentityProviderHandlerWrapper wrapper : identityProvider.getHandlers()) {
                handlers.add(wrapper.getHandler());
            }
        }

        this.getHandlerTabEditor().getHandlerTable().getDataProvider().setList(handlers);

        if (handlers.isEmpty()) {
            getHandlerTabEditor().enableDisableHandlerParameterActions(false);
        }

        this.getHandlerTabEditor().setIdentityProvider(identityProvider);

    }

    /**
     * @param federation
     */
    private void updateTrustedDomains(FederationWrapper federation) {
        List<TrustDomain> trustDomains = new ArrayList<TrustDomain>();
        
        for (IdentityProviderWrapper identityProvider : federation.getIdentityProviders()) {
            trustDomains.addAll(identityProvider.getTrustDomains());
        }

        this.getTrustedDomainTabEditor().getTrustDomainTable().getDataProvider().setList(trustDomains);
        
        IdentityProvider identityProvider = null;
        
        if (federation.getIdentityProvider() != null) {
            identityProvider = federation.getIdentityProvider().getIdentityProvider();
        }
        
        this.getTrustedDomainTabEditor().setIdentityProvider(identityProvider);
    }

    private void updateSelectedIdentityProvider(IdentityProvider identityProvider) {
        for (IdentityProviderWrapper identityProviderWrapper : getPresenter().getCurrentFederation().getIdentityProviders()) {
            if (identityProviderWrapper.getIdentityProvider().getName().equals(identityProvider.getName())) {
                this.selectedIdentityProvider = identityProviderWrapper;
                break;
            }
        }
    }

}