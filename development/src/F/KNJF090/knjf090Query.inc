<?php

require_once('for_php7.php');

class knjf090Query extends Query {

        //保健室利用者データを取得する
        function selectQuery($model)
        {
                $query  ="SELECT";
                $query .="    T1.SCHREGNO,";
                $query .="    DATE(T1.DATE) DATE,";
                $query .="    TIME(T1.DATE) TIME,";
                $query .="    substr(char(T1.OCCURTIME),1,2) OCCURTIME_H,";
                $query .="    substr(char(T1.OCCURTIME),4,2) OCCURTIME_M,";
                $query .="    substr(char(T1.BEDTIME),1,2) BEDTIME_H,";
                $query .="    substr(char(T1.BEDTIME),4,2) BEDTIME_M,";
                $query .="    substr(char(T1.RISINGTIME),1,2) RISINGTIME_H,";
                $query .="    substr(char(T1.RISINGTIME),4,2) RISINGTIME_M,";
                $query .="    T1.TREATMENT_DIV TREATMENT_DIV_CD,";
                $query .="    T1.TEMPERATURE,";
                $query .="    T1.PERIOD        || '：' || L0.NAME1 AS PERIOD ,";
                $query .="    T1.TREATMENT_DIV || '：' || L1.NAME1 AS TREATMENT_DIV,";
                $query .="    T1.VISIT_REASON  || '：' || (CASE T1.TREATMENT_DIV WHEN '01' THEN L2.NAME1 ELSE L3.NAME1 END) AS VISIT_REASON,";
                $query .="    T1.OCCURTIMECD   || '：' || L4.NAME1 AS OCCURTIMECD,";
                $query .="    T1.BREAKFAST     || '：' || L5.NAME1 AS BREAKFAST,";
                $query .="    T1.SLEEPING      || '：' || L6.NAME1 AS SLEEPING,";
                $query .="    T1.NURSETREAT    || '：' || L7.NAME1 AS NURSETREAT,";
                $query .="    T1.REMARK ";
                $query .="FROM ";
                $query .="    NURSEOFFICE_DAT T1 ";
                $query .="LEFT JOIN NAME_MST L0 ON L0.NAMECD1 = 'F700' ";
                $query .="                     AND L0.NAMECD2 = T1.PERIOD ";
                $query .="LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'F710' ";
                $query .="                     AND L1.NAMECD2 = T1.TREATMENT_DIV ";
                $query .="LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F720' ";
                $query .="                     AND L2.NAMECD2 = T1.VISIT_REASON ";
                $query .="LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'F730' ";
                $query .="                     AND L3.NAMECD2 = T1.VISIT_REASON ";
                $query .="LEFT JOIN NAME_MST L4 ON L4.NAMECD1 = 'F740' ";
                $query .="                     AND L4.NAMECD2 = T1.TREATMENT_DIV ";
                $query .="LEFT JOIN NAME_MST L5 ON L5.NAMECD1 = 'F750' ";
                $query .="                     AND L5.NAMECD2 = T1.BREAKFAST ";
                $query .="LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'F760' ";
                $query .="                     AND L6.NAMECD2 = T1.SLEEPING ";
                $query .="LEFT JOIN NAME_MST L7 ON L7.NAMECD1 = 'F770' ";
                $query .="                     AND L7.NAMECD2 = T1.NURSETREAT ";
                $query .="WHERE ";
                $query .="    T1.SCHREGNO = '" .$model->schregno ."' AND ";
                if ($model->cmd != "main")
                {
                        if (isset($model->date) && isset($model->time) && isset($model->treatment_div))
                        {
                                $query .= "  T1.DATE = TIMESTAMP('" .str_replace("/","-",$model->date) ."','".$model->time ."') AND ";
                        }else{
                                $query .= "  T1.DATE IS NULL AND ";
                        }
                        $query .= "  T1.TREATMENT_DIV = '" .$model->treatment_div ."' AND ";
                }
                $query .="    T1.YEAR = '" .$model->control_data["年度"] ."' ";
                $query .="ORDER BY";
                $query .="    T1.DATE";
//echo $query;
                return $query;
        }

        //名称マスタから名称取得
        function getV_name_mst($namecd,$model)
        {
            $query  = "SELECT * FROM V_NAME_MST ";
            $query .= "WHERE NAMECD1 = '".$namecd."' AND ";
            $query .= "      YEAR = '".CTRL_YEAR."'";

            return $query;
        }
        /**
        * INSERT
        */
        function &getInsertQuery($model)
        {
                $db = Query::dbCheckOut();

                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["YEAR"][TEXT]             = $model->control_data["年度"];
                $data["DATE"][TEXT]             = str_replace("/", "-", $model->field["DATE"]) ." " .sprintf("%02d:%02d:00", $model->field["DATE_H"], $model->field["DATE_M"]);
                $data["PERIOD"][TEXT]           = $model->field["PERIOD"];
                $data["TREATMENT_DIV"][TEXT]    = $model->field["TREATMENT_DIV"];
                $data["VISIT_REASON"][TEXT]     = $model->field["VISIT_REASON"];
                $data["OCCURTIMECD"][TEXT]      = $model->field["OCCURTIMECD"];
                if (is_numeric($model->field["OCCURTIME_H"]) && is_numeric($model->field["OCCURTIME_M"])){
                        $data["OCCURTIME"][TEXT]        = sprintf("%02d:%02d:00", $model->field["OCCURTIME_H"], $model->field["OCCURTIME_M"]);
                }
                $data["TEMPERATURE"][NUMBER]    = $model->field["TEMPERATURE"];
                $data["BREAKFAST"][TEXT]        = $model->field["BREAKFAST"];
                if (is_numeric($model->field["BEDTIME_H"]) && is_numeric($model->field["BEDTIME_M"])){
                        $data["BEDTIME"][TEXT]  = sprintf("%02d:%02d:00", $model->field["BEDTIME_H"], $model->field["BEDTIME_M"]);
                }
                if (is_numeric($model->field["RISINGTIME_H"]) && is_numeric($model->field["RISINGTIME_M"])){
                        $data["RISINGTIME"][TEXT]       = sprintf("%02d:%02d:00", $model->field["RISINGTIME_H"], $model->field["RISINGTIME_M"]);
                }
                $data["SLEEPING"][TEXT]         = $model->field["SLEEPING"];
                $data["NURSETREAT"][TEXT]       = $model->field["NURSETREAT"];
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]            = "SYSDATE()";

                $query = Query::insertSQL($data, "NURSEOFFICE_DAT");
                //insert文作成
                $db->query($query);
                Query::dbCheckIn($db);
        }

        /**
        * UPDATE
        */
        function &getUpdateQuery($model)
        {
                $db = Query::dbCheckOut();

                $data["PERIOD"][TEXT]           = $model->field["PERIOD"];
                $data["VISIT_REASON"][TEXT]     = $model->field["VISIT_REASON"];
                $data["OCCURTIMECD"][TEXT]      = $model->field["OCCURTIMECD"];
                if (is_numeric($model->field["OCCURTIME_H"]) && is_numeric($model->field["OCCURTIME_M"])){
                        $data["OCCURTIME"][TEXT]        = sprintf("%02d:%02d:00", $model->field["OCCURTIME_H"], $model->field["OCCURTIME_M"]);
                }else{
                        $data["OCCURTIME"][FUNC]        = "NULL";
                }
                $data["TEMPERATURE"][NUMBER]    = $model->field["TEMPERATURE"];
                $data["BREAKFAST"][TEXT]        = $model->field["BREAKFAST"];
                if (is_numeric($model->field["BEDTIME_H"]) && is_numeric($model->field["BEDTIME_M"])){
                        $data["BEDTIME"][TEXT]  = sprintf("%02d:%02d:00", $model->field["BEDTIME_H"], $model->field["BEDTIME_M"]);
                }else{
                        $data["BEDTIME"][FUNC]  = "NULL";
                }
                if (is_numeric($model->field["RISINGTIME_H"]) && is_numeric($model->field["RISINGTIME_M"])){
                        $data["RISINGTIME"][TEXT]       = sprintf("%02d:%02d:00", $model->field["RISINGTIME_H"], $model->field["RISINGTIME_M"]);
                }else{
                        $data["RISINGTIME"][FUNC]       = "NULL";
                }
                $data["SLEEPING"][TEXT]         = $model->field["SLEEPING"];
                $data["NURSETREAT"][TEXT]       = $model->field["NURSETREAT"];
                $data["REMARK"][TEXT]           = $model->field["REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data[UPDATED][FUNC]            = "SYSDATE()";

                $where = " WHERE SCHREGNO = '" .$model->schregno ."' AND ";
                $where .= "  DATE = TIMESTAMP('" .str_replace("/","-",$model->date) ."','".$model->time ."') AND";
                $where .= "  TREATMENT_DIV = '" .$model->treatment_div ."' ";

                $query = Query::updateSQL($data, "NURSEOFFICE_DAT", $where);
                //insert文作成
                $db->query($query);
                Query::dbCheckIn($db);

                return ;
        }
        /**
        * DELETE
        */
        function &getDeleteQuery($model)
        {
                $db = Query::dbCheckOut();

                $query = " DELETE FROM NURSEOFFICE_DAT";
                $query .= " WHERE SCHREGNO = '" .$model->schregno ."' AND ";
                $query .= "  DATE = TIMESTAMP('" .str_replace("/","-",$model->date) ."','".$model->time ."') AND ";
                $query .= "  TREATMENT_DIV = '" .$model->treatment_div ."' ";
                //insert文作成
                $db->query($query);

                Query::dbCheckIn($db);

                return ;
        }
}
?>
