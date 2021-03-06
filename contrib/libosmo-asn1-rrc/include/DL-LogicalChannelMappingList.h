/*
 * Generated by asn1c-0.9.24 (http://lionet.info/asn1c)
 * From ASN.1 module "InformationElements"
 * 	found in "../asn/InformationElements.asn"
 * 	`asn1c -fcompound-names -fnative-types`
 */

#ifndef	_DL_LogicalChannelMappingList_H_
#define	_DL_LogicalChannelMappingList_H_


#include <asn_application.h>

/* Including external dependencies */
#include <asn_SEQUENCE_OF.h>
#include <constr_SEQUENCE_OF.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Forward declarations */
struct DL_LogicalChannelMapping;

/* DL-LogicalChannelMappingList */
typedef struct DL_LogicalChannelMappingList {
	A_SEQUENCE_OF(struct DL_LogicalChannelMapping) list;
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} DL_LogicalChannelMappingList_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_DL_LogicalChannelMappingList;

#ifdef __cplusplus
}
#endif

/* Referred external types */
#include "DL-LogicalChannelMapping.h"

#endif	/* _DL_LogicalChannelMappingList_H_ */
#include <asn_internal.h>
