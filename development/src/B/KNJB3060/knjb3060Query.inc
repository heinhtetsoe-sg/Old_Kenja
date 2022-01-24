<?php

require_once('for_php7.php');
class knjb3060Query extends Query {

    //基本時間割タイトル
    function getTemplateData($model){
        $query  = " SELECT ";
        $query .= "     SPH.BSCSEQ, ";
        $query .= "     SPH.UPDATED, ";
        $query .= "     SPH.TITLE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT SPH ";
        $query .= " WHERE ";
        $query .= "     SPH.YEAR = '{$model->year}' ";
        $query .= "     AND SPH.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->selectSchoolKind || $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SPH.BSCSEQ IN (SELECT DISTINCT T1.BSCSEQ FROM SCH_PTRN_DAT T1 ";
            $query .= "       INNER JOIN CHAIR_DAT CH ON CH.YEAR = T1.YEAR ";
            $query .= "           AND CH.SEMESTER = T1.SEMESTER ";
            $query .= "           AND CH.CHAIRCD = T1.CHAIRCD ";
            $query .= "       WHERE ";
            $query .= "         T1.YEAR = '{$model->year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND CH.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND CH.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SPH.BSCSEQ ";

        return $query;
    }
    
    //履歴
    function getRirekiData($model){
        $query  = " SELECT ";
        $query .= "     D1.REFLECTDATE, ";
        $query .= "     D1.REFLECTDIV, ";
        $query .= "     D1.SDATE, ";
        $query .= "     D1.EDATE, ";
        $query .= "     D1.BSCSEQ, ";
        $query .= "     D1.DAYCD, ";
        $query .= "     D1.DAYS, ";
        $query .= "     SPH.UPDATED, ";
        $query .= "     SPH.TITLE ";
        $query .= " FROM ";
        $query .= "     PTRN_REFLECTHIST_DAT D1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCH_PTRN_HDAT SPH ";
        $query .= "     ON D1.BSCSEQ = SPH.BSCSEQ ";
        $query .= "     AND D1.YEAR = SPH.YEAR ";
        $query .= " WHERE ";
        $query .= "     D1.YEAR = '{$model->year}' ";
        $query .= "     AND SPH.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->selectSchoolKind || $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SPH.BSCSEQ IN (SELECT DISTINCT T1.BSCSEQ FROM SCH_PTRN_DAT T1 ";
            $query .= "       INNER JOIN CHAIR_DAT CH ON CH.YEAR = T1.YEAR ";
            $query .= "           AND CH.SEMESTER = T1.SEMESTER ";
            $query .= "           AND CH.CHAIRCD = T1.CHAIRCD ";
            $query .= "       WHERE ";
            $query .= "         T1.YEAR = '{$model->year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND CH.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND CH.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     D1.REFLECTDATE DESC ";

        return $query;
    }
    
    //学期
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";

        return $query;
    }
    
    //対象範囲の開始日時
    function getRengeStert($model, $semeSdate, $semeEdate) {
        $query .= " WITH MAX_T AS (SELECT ";
        $query .= "     MAX(EXECUTEDATE) AS EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTED = '1' ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND  = '".SCHOOLKIND."') ";
        }
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     MAX(EXECUTEDATE) AS EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTED = '1' ";
        $query .= "     AND EXECUTEDATE BETWEEN '{$semeSdate}' AND '{$semeEdate}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND  = '".SCHOOLKIND."') ";
        }
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     MAX(EXECUTEDATE) ";
        $query .= " FROM ";
        $query .= "     MAX_T ";
        
        return $query;
    }

    //更新
    function &getUpdateQuery($model) {
        if($model->reflectDiv == 3){
            if($model->weekTableDays == ''){
                return;
            }
            $dayListTemp = explode(',', str_replace('/','-',$model->weekTableDays));
        } else {
            if($model->startDate == '' || $model->endDate == ''){
                return;
            }
            $formatStartData = str_replace('/','-',$model->startDate);
            $formatEndData = str_replace('/','-',$model->endDate);
            $sData = strtotime($model->startDate);
            $eData = strtotime($model->endDate);
            $flag = 1;
            for($currentData = $sData;$currentData<=$eData;$currentData += 24*60*60){
                if($model->reflectDiv == 0){
                    $flag = 1;
                }
                if($model->reflectDiv == 1){
                    if($currentData != $sData){
                        if(date('w',$currentData) == 1){
                            $flag = ($flag == 0) ? 1 : 0;
                        }
                    }
                }
                if($model->reflectDiv == 2){
                    if($currentData != $sData){
                        if(date('w',$currentData) == 1){
                            if($flag == 0){
                                $flag = 1;
                            } else if($flag == 1){
                                $flag = 2;
                            } else if($flag == 2){
                                $flag = 0;
                            }
                        }
                    }
                }
                if($flag == 1){
                    $dayListTemp[] = date('Y-m-d',$currentData);
                }
            }
        }
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $dayList = array();
        $holiday = array();
        $result = $db->query(" SELECT * FROM HOLIDAY_MST ");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holiday[$row['HOLIDAY']] = true;
        }
        for ($i = 0; $i < get_count($dayListTemp); $i++) {
            if(!$holiday[$dayListTemp[$i]]){
                $dayList[] = $dayListTemp[$i];
            }
        }

        $chairWhere = "";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $chairWhere .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $chairWhere .= " AND CHAIRCD IN (SELECT CHAIRCD FROM CHAIR_DAT WHERE YEAR = '{$model->year}' AND SEMESTER = '{$model->semester}' AND SCHOOL_KIND  = '".SCHOOLKIND."') ";
        }
        
        if(get_count($dayList) != 0){
            $query  = " DELETE FROM SCH_CHR_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            if($model->reflectDiv == 3){
                $query .= "     AND EXECUTEDATE IN ('".join("','", $dayList)."') ";
            } else {
                $query .= "     AND EXECUTEDATE BETWEEN '{$formatStartData}' AND '{$formatEndData}' ";
            }
            $query .= $chairWhere;
            $db->query($query);
            
            //SCH_PTRN_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->bscSeq} ";
            if($model->reflectDiv == 3){
                $query .= "     AND DAYCD = '{$model->weekRadioCalc}' ";
            }
            $query .= $chairWhere;
            $result = $db->query($query);
            
            $rec = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rec[$row['DAYCD']][$row['PERIODCD']][$row['CHAIRCD']] = 1;
            }
            
            for($i=0;$i<get_count($dayList);$i++){
                if($model->reflectDiv == 3){
                    $dayCd = $model->weekRadioCalc;
                } else {
                    $dayCd = date('w',strtotime($dayList[$i]))+1;
                }
                if(!isset($rec[$dayCd])){
                    continue;
                }
                foreach($rec[$dayCd] as $periodCd => $chairCdList){
                    foreach($chairCdList as $chairCd => $value){
                        $data = array();
                        $data["EXECUTEDATE"][DATE]  = $dayList[$i];
                        $data["PERIODCD"][TEXT]     = $periodCd;
                        $data["CHAIRCD"][TEXT]      = $chairCd;
                        $data["EXECUTED"][TEXT]     = 0;
                        $data["EXECUTEDIV"][TEXT]   = 0;
                        $data["DATADIV"][TEXT]      = 0;
                        $data["YEAR"][TEXT]         = $model->year;
                        $data["SEMESTER"][TEXT]     = $model->semester;
                        $data["ATTESTOR"][TEXT]     = null;
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][NUMBER]    = "sysdate()";

                        $query = Query::insertSQL($data, "SCH_CHR_DAT");
                        $db->query($query);
                    }
                }
            }
            
            $query  = " DELETE FROM SCH_FAC_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            if($model->reflectDiv == 3){
                $query .= "     AND EXECUTEDATE IN ('".join("','", $dayList)."') ";
            } else {
                $query .= "     AND EXECUTEDATE BETWEEN '{$formatStartData}' AND '{$formatEndData}' ";
            }
            $query .= $chairWhere;
            $db->query($query);
            
            
            //SCH_PTRN_FAC_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_FAC_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->bscSeq} ";
            if($model->reflectDiv == 3){
                $query .= "     AND DAYCD = '{$model->weekRadioCalc}' ";
            }
            $query .= $chairWhere;
            $result = $db->query($query);
            
            $rec = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rec[$row['DAYCD']][$row['PERIODCD']][$row['CHAIRCD']][$row['FACCD']] = 1;
            }
            
            for($i=0;$i<get_count($dayList);$i++){
                if($model->reflectDiv == 3){
                    $dayCd = $model->weekRadioCalc;
                } else {
                    $dayCd = date('w',strtotime($dayList[$i]))+1;
                }
                if(!isset($rec[$dayCd])){
                    continue;
                }
                foreach($rec[$dayCd] as $periodCd => $chairCdList){
                    foreach($chairCdList as $chairCd => $facCdList){
                        foreach($facCdList as $facCd => $value){
                            $data = array();
                            $data["EXECUTEDATE"][TEXT]    = $dayList[$i];
                            $data["PERIODCD"][TEXT]       = $periodCd;
                            $data["CHAIRCD"][TEXT]        = $chairCd;
                            $data["FACCD"][TEXT]          = $facCd;
                            $data["YEAR"][TEXT]           = $model->year;
                            $data["SEMESTER"][TEXT]       = $model->semester;
                            $data["REGISTERCD"][TEXT]     = STAFFCD;
                            $data["UPDATED"][NUMBER]      = "sysdate()";

                            $query = Query::insertSQL($data, "SCH_FAC_DAT");
                            $db->query($query);
                        }
                    }
                }
            }
            
            $query  = " DELETE FROM SCH_CHR_COUNTFLG WHERE ";
            if($model->reflectDiv == 3){
                $query .= "     EXECUTEDATE IN ('".join("','", $dayList)."') ";
            } else {
                $query .= "     EXECUTEDATE BETWEEN '{$formatStartData}' AND '{$formatEndData}' ";
            }
            $query .= $chairWhere;
            $db->query($query);
            
            
            //SCH_PTRN_COUNTFLG_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_COUNTFLG_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->bscSeq} ";
            if($model->reflectDiv == 3){
                $query .= "     AND DAYCD = '{$model->weekRadioCalc}' ";
            }
            $query .= $chairWhere;
            $result = $db->query($query);
            
            $rec = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rec[$row['DAYCD']][$row['PERIODCD']][$row['CHAIRCD']][$row['GRADE']][$row['HR_CLASS']] = $row;
            }
            
            for($i=0;$i<get_count($dayList);$i++){
                if($model->reflectDiv == 3){
                    $dayCd = $model->weekRadioCalc;
                } else {
                    $dayCd = date('w',strtotime($dayList[$i]))+1;
                }
                if(!isset($rec[$dayCd])){
                    continue;
                }
                foreach($rec[$dayCd] as $periodCd => $chairCdList){
                    foreach($chairCdList as $chairCd => $gradeCdList){
                        foreach($gradeCdList as $gradeCd => $hrClassCdList){
                            foreach($hrClassCdList as $hrClassCd => $value){
                            
                                $data = array();
                                $data["EXECUTEDATE"][TEXT]  = $dayList[$i];
                                $data["PERIODCD"][TEXT]     = $periodCd;
                                $data["CHAIRCD"][TEXT]      = $chairCd;
                                $data["GRADE"][TEXT]        = $gradeCd;
                                $data["HR_CLASS"][TEXT]     = $hrClassCd;
                                $data["COUNTFLG"][TEXT]     = $value['COUNTFLG'];
                                $data["LESSON_MODE"][TEXT]  = '00';
                                $data["REGISTERCD"][TEXT]   = STAFFCD;
                                $data["UPDATED"][NUMBER]    = "sysdate()";
                                $query = Query::insertSQL($data, "SCH_CHR_COUNTFLG");
                                $db->query($query);
                            }
                        }
                    }
                }
            }
            
            $query  = " DELETE FROM SCH_STF_DAT WHERE ";
            if($model->reflectDiv == 3){
                $query .= "     EXECUTEDATE IN ('".join("','", $dayList)."') ";
            } else {
                $query .= "     EXECUTEDATE BETWEEN '{$formatStartData}' AND '{$formatEndData}' ";
            }
            $query .= $chairWhere;
            $db->query($query);
            
            
            //SCH_PTRN_STF_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->bscSeq} ";
            if($model->reflectDiv == 3){
                $query .= "     AND DAYCD = '{$model->weekRadioCalc}' ";
            }
            $query .= $chairWhere;
            $result = $db->query($query);
            
            $rec = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rec[$row['DAYCD']][$row['PERIODCD']][$row['CHAIRCD']][$row['STAFFCD']] = 1;
            }
            
            for($i=0;$i<get_count($dayList);$i++){
                if($model->reflectDiv == 3){
                    $dayCd = $model->weekRadioCalc;
                } else {
                    $dayCd = date('w',strtotime($dayList[$i]))+1;
                }
                if(!isset($rec[$dayCd])){
                    continue;
                }
                foreach($rec[$dayCd] as $periodCd => $chairCdList){
                    foreach($chairCdList as $chairCd => $staffCdList){
                        foreach($staffCdList as $staffCd => $value){
                            $data = array();
                            $data["EXECUTEDATE"][TEXT]     = $dayList[$i];
                            $data["PERIODCD"][TEXT]        = $periodCd;
                            $data["CHAIRCD"][TEXT]         = $chairCd;
                            $data["STAFFCD"][TEXT]         = $staffCd;
                            $data["REGISTERCD"][TEXT]      = STAFFCD;
                            $data["UPDATED"][NUMBER]       = "sysdate()";

                            $query = Query::insertSQL($data, "SCH_STF_DAT");
                            $db->query($query);
                        }
                    }
                }
            }
            
        }
        
        $week = array('日','月','火','水','木','金','土');
        $daysList = explode(',', $model->weekTableDays);
        for($i=0;$i<get_count($daysList);$i++){
            $daysList[$i] = substr($daysList[$i],5).'('.$week[date('w', strtotime($daysList[$i]))].')';
        }
        $daysText = join(',',$daysList);
        
        //PTRN_REFLECTHIST_DAT
        $data = array();
        $data["REFLECTDATE"][NUMBER] = "sysdate()";
        $data["REFLECTDIV"][TEXT]    = $model->reflectDiv;
        $data["SDATE"][TEXT]         = ($model->reflectDiv != 3)?$formatStartData:'';
        $data["EDATE"][TEXT]         = ($model->reflectDiv != 3)?$formatEndData:'';
        $data["YEAR"][TEXT]          = $model->year;
        $data["BSCSEQ"][NUMBER]      = $model->bscSeq;
        $data["DAYCD"][TEXT]         = ($model->reflectDiv != 3)?'':$model->weekRadio;
        $data["DAYS"][TEXT]          = ($model->reflectDiv != 3)?'':$daysText;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";

        $query = Query::insertSQL($data, "PTRN_REFLECTHIST_DAT");
        $db->query($query);
        
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
