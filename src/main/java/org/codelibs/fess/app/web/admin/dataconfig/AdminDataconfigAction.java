/*
 * Copyright 2009-2015 the CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.codelibs.fess.app.web.admin.dataconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.codelibs.fess.Constants;
import org.codelibs.fess.app.pager.DataConfigPager;
import org.codelibs.fess.app.service.DataConfigService;
import org.codelibs.fess.app.service.LabelTypeService;
import org.codelibs.fess.app.service.RoleTypeService;
import org.codelibs.fess.app.web.CrudMode;
import org.codelibs.fess.app.web.base.FessAdminAction;
import org.codelibs.fess.ds.DataStoreFactory;
import org.codelibs.fess.es.exentity.DataConfig;
import org.codelibs.fess.helper.SystemHelper;
import org.dbflute.optional.OptionalEntity;
import org.lastaflute.web.Execute;
import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.response.render.RenderData;
import org.lastaflute.web.token.TxToken;
import org.lastaflute.web.validation.VaErrorHook;

/**
 * @author codelibs
 * @author Keiichi Watanabe
 */
public class AdminDataconfigAction extends FessAdminAction {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private DataConfigService dataConfigService;
    @Resource
    private DataConfigPager dataConfigPager;
    @Resource
    private RoleTypeService roleTypeService;
    @Resource
    private LabelTypeService labelTypeService;
    @Resource
    protected DataStoreFactory dataStoreFactory;
    @Resource
    private SystemHelper systemHelper;

    // ===================================================================================
    //                                                                               Hook
    //                                                                              ======
    @Override
    protected void setupHtmlData(final ActionRuntime runtime) {
        super.setupHtmlData(runtime);
        runtime.registerData("helpLink", systemHelper.getHelpLink("dataConfig"));
    }

    // ===================================================================================
    //                                                                      Search Execute
    //                                                                      ==============
    @Execute
    public HtmlResponse index(final SearchForm form) {
        return asHtml(path_AdminDataconfig_IndexJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse list(final Integer pageNumber, final SearchForm form) {
        dataConfigPager.setCurrentPageNumber(pageNumber);
        return asHtml(path_AdminDataconfig_IndexJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse search(final SearchForm form) {
        copyBeanToBean(form, dataConfigPager, op -> op.exclude(Constants.PAGER_CONVERSION_RULE));
        return asHtml(path_AdminDataconfig_IndexJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse reset(final SearchForm form) {
        dataConfigPager.clear();
        return asHtml(path_AdminDataconfig_IndexJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    @Execute
    public HtmlResponse back(final SearchForm form) {
        return asHtml(path_AdminDataconfig_IndexJsp).renderWith(data -> {
            searchPaging(data, form);
        });
    }

    protected void searchPaging(final RenderData data, final SearchForm form) {
        data.register("dataConfigItems", dataConfigService.getDataConfigList(dataConfigPager)); // page navi

        // restore from pager
        copyBeanToBean(dataConfigPager, form, op -> op.include("id"));
    }

    // ===================================================================================
    //                                                                        Edit Execute
    //                                                                        ============
    // -----------------------------------------------------
    //                                            Entry Page
    //                                            ----------
    @Execute(token = TxToken.SAVE)
    public HtmlResponse createpage() {
        return asHtml(path_AdminDataconfig_EditJsp).useForm(CreateForm.class, op -> {
            op.setup(form -> {
                form.initialize();
                form.crudMode = CrudMode.CREATE;
            });
        }).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse editpage(final int crudMode, final String id) {
        verifyCrudMode(crudMode, CrudMode.EDIT);
        return asHtml(path_AdminDataconfig_EditJsp).useForm(EditForm.class, op -> {
            op.setup(form -> {
                dataConfigService.getDataConfig(id).ifPresent(entity -> {
                    copyBeanToBean(entity, form, copyOp -> {
                        copyOp.excludeNull();
                    });
                }).orElse(() -> {
                    throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
                });
                form.crudMode = crudMode;
            });
        }).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse createagain(final CreateForm form) {
        verifyCrudMode(form.crudMode, CrudMode.CREATE);
        validate(form, messages -> {}, toEditHtml());
        return asHtml(path_AdminDataconfig_EditJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse editagain(final EditForm form) {
        verifyCrudMode(form.crudMode, CrudMode.EDIT);
        validate(form, messages -> {}, toEditHtml());
        return asHtml(path_AdminDataconfig_EditJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse editfromconfirm(final EditForm form) {
        validate(form, messages -> {}, toEditHtml());
        form.crudMode = CrudMode.EDIT;
        final String id = form.id;
        dataConfigService.getDataConfig(id).ifPresent(entity -> {
            copyBeanToBean(entity, form, op -> {});
        }).orElse(() -> {
            throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
        });
        return asHtml(path_AdminDataconfig_EditJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse deletepage(final int crudMode, final String id) {
        verifyCrudMode(crudMode, CrudMode.DELETE);
        return asHtml(path_AdminDataconfig_ConfirmJsp).useForm(EditForm.class, op -> {
            op.setup(form -> {
                dataConfigService.getDataConfig(id).ifPresent(entity -> {
                    copyBeanToBean(entity, form, copyOp -> {
                        copyOp.excludeNull();
                    });
                }).orElse(() -> {
                    throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
                });
                form.crudMode = crudMode;
            });
        }).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.SAVE)
    public HtmlResponse deletefromconfirm(final EditForm form) {
        validate(form, messages -> {}, toEditHtml());
        form.crudMode = CrudMode.DELETE;
        final String id = form.id;
        dataConfigService.getDataConfig(id).ifPresent(entity -> {
            copyBeanToBean(entity, form, op -> {});
        }).orElse(() -> {
            throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
        });
        return asHtml(path_AdminDataconfig_ConfirmJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    // -----------------------------------------------------
    //                                               Confirm
    //                                               -------
    @Execute
    public HtmlResponse confirmpage(final int crudMode, final String id) {
        verifyCrudMode(crudMode, CrudMode.CONFIRM);
        return asHtml(path_AdminDataconfig_ConfirmJsp).useForm(EditForm.class, op -> {
            op.setup(form -> {
                dataConfigService.getDataConfig(id).ifPresent(entity -> {
                    copyBeanToBean(entity, form, copyOp -> {
                        copyOp.excludeNull();
                    });
                    form.crudMode = crudMode;
                }).orElse(() -> {
                    throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
                });
            });
        }).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.VALIDATE_KEEP)
    public HtmlResponse confirmfromcreate(final CreateForm form) {
        validate(form, messages -> {}, toEditHtml());
        form.crudMode = CrudMode.CREATE;
        return asHtml(path_AdminDataconfig_ConfirmJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    @Execute(token = TxToken.VALIDATE_KEEP)
    public HtmlResponse confirmfromupdate(final EditForm form) {
        validate(form, messages -> {}, toEditHtml());
        form.crudMode = CrudMode.EDIT;
        return asHtml(path_AdminDataconfig_ConfirmJsp).renderWith(data -> {
            registerRolesAndLabels(data);
            registerHandlerNames(data);
        });
    }

    // -----------------------------------------------------
    //                                         Actually Crud
    //                                         -------------
    @Execute(token = TxToken.VALIDATE)
    public HtmlResponse create(final CreateForm form) {
        verifyCrudMode(form.crudMode, CrudMode.CREATE);
        validate(form, messages -> {}, toEditHtml());
        createDataConfig(form).ifPresent(entity -> {
            copyBeanToBean(form, entity, op -> op.exclude(Constants.COMMON_CONVERSION_RULE));
            dataConfigService.store(entity);
            saveInfo(messages -> messages.addSuccessCrudCreateCrudTable(GLOBAL));
        }).orElse(() -> {
            throwValidationError(messages -> messages.addErrorsCrudFailedToCreateCrudTable(GLOBAL), toEditHtml());
        });
        return redirect(getClass());
    }

    @Execute(token = TxToken.VALIDATE)
    public HtmlResponse update(final EditForm form) {
        verifyCrudMode(form.crudMode, CrudMode.EDIT);
        validate(form, messages -> {}, toEditHtml());
        createDataConfig(form).ifPresent(entity -> {
            copyBeanToBean(form, entity, op -> op.exclude(Constants.COMMON_CONVERSION_RULE));
            dataConfigService.store(entity);
            saveInfo(messages -> messages.addSuccessCrudUpdateCrudTable(GLOBAL));
        }).orElse(() -> {
            throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, form.id), toEditHtml());
        });
        return redirect(getClass());
    }

    @Execute
    public HtmlResponse delete(final EditForm form) {
        verifyCrudMode(form.crudMode, CrudMode.DELETE);
        validate(form, messages -> {}, toEditHtml());
        final String id = form.id;
        dataConfigService.getDataConfig(id).ifPresent(entity -> {
            dataConfigService.delete(entity);
            saveInfo(messages -> messages.addSuccessCrudDeleteCrudTable(GLOBAL));
        }).orElse(() -> {
            throwValidationError(messages -> messages.addErrorsCrudCouldNotFindCrudTable(GLOBAL, id), toEditHtml());
        });
        return redirect(getClass());
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    protected OptionalEntity<DataConfig> createDataConfig(final CreateForm form) {
        final String username = systemHelper.getUsername();
        final long currentTime = systemHelper.getCurrentTimeAsLong();
        switch (form.crudMode) {
        case CrudMode.CREATE:
            if (form instanceof CreateForm) {
                final DataConfig entity = new DataConfig();
                entity.setCreatedBy(username);
                entity.setCreatedTime(currentTime);
                entity.setUpdatedBy(username);
                entity.setUpdatedTime(currentTime);
                return OptionalEntity.of(entity);
            }
            break;
        case CrudMode.EDIT:
            if (form instanceof EditForm) {
                return dataConfigService.getDataConfig(((EditForm) form).id).map(entity -> {
                    entity.setUpdatedBy(username);
                    entity.setUpdatedTime(currentTime);
                    return entity;
                });
            }
            break;
        default:
            break;
        }
        return OptionalEntity.empty();
    }

    protected void registerRolesAndLabels(final RenderData data) {
        data.register("roleTypeItems", roleTypeService.getRoleTypeList());
        data.register("labelTypeItems", labelTypeService.getLabelTypeList());
    }

    protected void registerHandlerNames(final RenderData data) {
        final List<String> dataStoreNameList = dataStoreFactory.getDataStoreNameList();
        final List<Map<String, String>> itemList = new ArrayList<Map<String, String>>();
        for (final String name : dataStoreNameList) {
            final Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.ITEM_LABEL, name);
            map.put(Constants.ITEM_VALUE, name);
            itemList.add(map);
        }
        data.register("handlerNameItems", itemList);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected void verifyCrudMode(final int crudMode, final int expectedMode) {
        if (crudMode != expectedMode) {
            throwValidationError(messages -> {
                messages.addErrorsCrudInvalidMode(GLOBAL, String.valueOf(expectedMode), String.valueOf(crudMode));
            }, toEditHtml());
        }
    }

    protected VaErrorHook toEditHtml() {
        return () -> {
            return asHtml(path_AdminDataconfig_EditJsp).renderWith(data -> {
                registerRolesAndLabels(data);
                registerHandlerNames(data);
            });
        };
    }
}