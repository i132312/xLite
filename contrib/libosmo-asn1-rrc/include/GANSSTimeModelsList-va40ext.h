/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_GANSSTimeModelsList_va40ext_H_
#define	_GANSSTimeModelsList_va40ext_H_


#include <asn_application.h>

/* Including external dependencies */
#include <asn_SEQUENCE_OF.h>
#include <constr_SEQUENCE_OF.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Forward declarations */
struct UE_Positioning_GANSS_TimeModel_va40ext;

/* GANSSTimeModelsList-va40ext */
typedef struct GANSSTimeModelsList_va40ext {
	A_SEQUENCE_OF(struct UE_Positioning_GANSS_TimeModel_va40ext) list;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} GANSSTimeModelsList_va40ext_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_GANSSTimeModelsList_va40ext;

#ifdef __cplusplus
}
#endif

/* Referred external types */
#include "UE-Positioning-GANSS-TimeModel-va40ext.h"

#endif	/* _GANSSTimeModelsList_va40ext_H_ */
#include <asn_internal.h>
