import { IntegrationFormFieldsInterface } from 'moduleFederation/common';
import { FC, useEffect } from 'react';

import { LABELS } from '../constants';

const INTEGRATION_NAME = 'integrationName';
const URL = 'url';
const PROJECT = 'project';
const API_TOKEN = 'apiToken';
const MAX_LENGTH = 55;

const projectIdValidator =
  (requiredFieldValidator: (value: string) => string | undefined) => (value: string) => {
    if (requiredFieldValidator(value)) {
      return `Project ID should have length from 1 to ${MAX_LENGTH}`;
    } else if (!value.match(/^\d+$/)) {
      return 'Project ID should contain only digits';
    } else {
      return undefined;
    }
  };

export const IntegrationFormFields: FC<IntegrationFormFieldsInterface> = (props) => {
  const { initialize, disabled, lineAlign, initialData, updateMetaData, ...extensionProps } = props;
  const {
    components: { FieldErrorHint, FieldElement, FieldText, FieldTextFlex },
    validators: { requiredField, btsUrl, btsIntegrationName },
    constants: { SECRET_FIELDS_KEY },
  } = extensionProps;

  useEffect(() => {
    initialize(initialData);
    updateMetaData({
      [SECRET_FIELDS_KEY]: [API_TOKEN],
    });
  }, []);

  return (
    <>
      <FieldElement
        name={INTEGRATION_NAME}
        label={LABELS.INTEGRATION_NAME}
        validate={btsIntegrationName}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={MAX_LENGTH} defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement name={URL} label={LABELS.URL} validate={btsUrl} disabled={disabled} isRequired>
        <FieldErrorHint provideHint={false}>
          <FieldText defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={PROJECT}
        label={LABELS.PROJECT}
        validate={projectIdValidator(requiredField)}
        disabled={disabled}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldText maxLength={MAX_LENGTH} defaultWidth={false} />
        </FieldErrorHint>
      </FieldElement>
      <FieldElement
        name={API_TOKEN}
        label={LABELS.TOKEN}
        disabled={disabled}
        validate={requiredField}
        isRequired
      >
        <FieldErrorHint provideHint={false}>
          <FieldTextFlex />
        </FieldErrorHint>
      </FieldElement>
    </>
  );
};
