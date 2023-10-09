import { LABELS } from '../constants';

export const IntegrationSettings = (props: any) => {
  const { data, goToPreviousPage, onUpdate, isGlobal, ...extensionProps } = props;
  const {
    lib: { React, useDispatch },
    actions: { showModalAction, hideModalAction },
    components: {
      IntegrationSettings: IntegrationSettingsContainer,
      BtsAuthFieldsInfo,
      BtsPropertiesForIssueForm,
    },
    utils: { getDefectFormFields },
    constants: { BTS_FIELDS_FORM },
  } = extensionProps;

  const dispatch = useDispatch();

  const fieldsConfig = [
    {
      value: data.integrationParameters.url,
      message: LABELS.URL,
    },
    {
      value: data.integrationParameters.project,
      message: LABELS.PROJECT,
    },
  ];

  const getConfirmationFunc =
    (testConnection: any) => (integrationData: any, integrationMetaData: any) => {
      onUpdate(
        integrationData,
        () => {
          dispatch(hideModalAction());
          testConnection();
        },
        integrationMetaData
      );
    };

  const editAuthorizationClickHandler = (testConnection: any) => {
    const {
      data: { name, integrationParameters, integrationType },
    } = props;

    dispatch(
      showModalAction({
        id: 'createProjectIntegrationModal',
        data: {
          modalTitle: 'Edit authorization',
          onConfirm: getConfirmationFunc(testConnection),
          instanceType: integrationType.name,
          customProps: {
            initialData: {
              ...integrationParameters,
              integrationName: name,
            },
            editAuthMode: true,
          },
        },
      })
    );
  };

  const getEditAuthConfig = () => ({
    content: <BtsAuthFieldsInfo fieldsConfig={fieldsConfig} />,
    onClick: editAuthorizationClickHandler,
  });

  const onSubmit = (integrationData: any, callback: any, metaData: any) => {
    const { fields, checkedFieldsIds = {}, ...meta } = metaData;
    const defectFormFields = getDefectFormFields(fields, checkedFieldsIds, integrationData);

    onUpdate({ defectFormFields }, callback, meta);
  };

  return (
    <IntegrationSettingsContainer
      data={data}
      goToPreviousPage={goToPreviousPage}
      onUpdate={onSubmit}
      editAuthConfig={getEditAuthConfig()}
      isGlobal={isGlobal}
      formFieldsComponent={BtsPropertiesForIssueForm}
      formKey={BTS_FIELDS_FORM}
      isEmptyConfiguration={
        !data.integrationParameters.defectFormFields ||
        !data.integrationParameters.defectFormFields.length
      }
    />
  );
};
