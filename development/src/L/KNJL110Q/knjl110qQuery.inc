<?php

require_once('for_php7.php');
class knjl110qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        if ($namecd1 == 'L063') {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //対象者・合格者取得
    public function getList($model, $divLR)
    {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR     = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1  = 'L013' ";
        $query .= "                              AND L013.NAMECD2  = BASE.JUDGEMENT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S035 ON S035.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S035.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S035.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S035.SEQ          = '035' ";
        $query .= "     LEFT JOIN V_NAME_MST L063 ON L063.YEAR = BASE.ENTEXAMYEAR ";
        $query .= "                             AND L063.NAMECD1 = 'L063' ";
        $query .= "                             AND L063.NAMECD2 = S035.REMARK1 ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR            = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV           = '{$model->applicantdiv}' ";
        //合格者が対象
        $query .= "     AND L013.NAMESPARE1             = '1' ";
        //入学者が対象
        $query .= "     AND VALUE(BASE.ENTDIV,'')       = '1' "; //入学区分 1:済み 2:辞退
        //手続者が対象
        $query .= "     AND VALUE(BASE.PROCEDUREDIV,'') = '1' "; //手続区分 1:済み 2:無し（辞退）
        if ($divLR == 'LEFT') {
            $query .= "     AND S035.REMARK1                = '{$model->temporary_class}' ";
        } else {
            $query .= "     AND (S035.REMARK1               is null ";
            $query .= "          OR L063.NAMECD2 is null )";
        }
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        $attendNo = 1;

        foreach ($array as $examno) {
            //カウント取得(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
            $getCnt035 = $db->getOne(knjl110qQuery::getDetailCount($model, $examno, '035'));

            //仮クラス、出席番号をセット
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              = '035';
            $data["REMARK1"][TEXT]          = $model->temporary_class;
            $data["REMARK2"][TEXT]          = sprintf("%03d", $attendNo++);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($getCnt035 > 0) {
                $where  = " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO       = '{$examno}' ";
                $where .= "   AND SEQ          = '035' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);
        }

        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());

        foreach ($array2 as $examno) {
            //カウント取得(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
            $getCnt035 = $db->getOne(knjl110qQuery::getDetailCount($model, $examno, '035'));

            if ($getCnt035 == 0) {
                continue;
            }

            //仮クラス、出席番号をリセット
            $data = array();
            $data["REMARK1"][TEXT]          = '';
            $data["REMARK2"][TEXT]          = '';
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO       = '{$examno}' ";
            $where .= "   AND SEQ          = '035' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //ENTEXAM_APPLICANTBASE_DETAIL_DATカウント取得
    public function getDetailCount($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*)";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";
        $query .= "   AND SEQ           = '{$seq}' ";

        return $query;
    }

    /**********/
    /* ＣＳＶ */
    /**********/
    //データ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     BASE.ENTEXAMYEAR, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FINS.FINSCHOOL_NAME, ";
        $query .= "     '' as TEMPORARY_CLASS, ";
        $query .= "     '".$model->lastColumn."' as ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON FINS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ON L013.YEAR     = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L013.NAMECD1  = 'L013' ";
        $query .= "                              AND L013.NAMECD2  = BASE.JUDGEMENT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S035 ON S035.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                    AND S035.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                    AND S035.EXAMNO       = BASE.EXAMNO ";
        $query .= "                                                    AND S035.SEQ          = '035' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR            = '{$model->ObjYear}' ";
        $query .= "     AND BASE.APPLICANTDIV           = '{$model->applicantdiv}' ";
        //合格者が対象
        $query .= "     AND L013.NAMESPARE1             = '1' ";
        //入学者が対象
        $query .= "     AND VALUE(BASE.ENTDIV,'')       = '1' "; //入学区分 1:済み 2:辞退
        //手続者が対象
        $query .= "     AND VALUE(BASE.PROCEDUREDIV,'') = '1' "; //手続区分 1:済み 2:無し（辞退）
        $query .= "     AND S035.REMARK1               is null ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //ＣＳＶデータ読込
    public function udpateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $attendNoArr = array();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $setClassKey = $data_arr[$i]["TEMPORARY_CLASS"];
            //仮クラスを持つデータをリセット
            if ($attendNoArr[$setClassKey] == '') {
                $data = array();
                $data["REMARK1"][TEXT]         = '';
                $data["REMARK2"][TEXT]         = '';

                $where  = " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "   AND SEQ          = '035' ";
                $where .= "   AND REMARK1      = '{$setClassKey}' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);

                $attendNoArr[$setClassKey] = 1;
            }

            //カウント取得(ENTEXAM_APPLICANTBASE_DETAIL_DAT)
            $getCnt035 = $db->getOne(knjl110qQuery::getDetailCount($model, $data_arr[$i]["EXAMNO"], '035'));

            //仮クラス、出席番号をセット
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = '035';
            $data["REMARK1"][TEXT]          = $setClassKey;
            $data["REMARK2"][TEXT]          = sprintf("%03d", $attendNoArr[$setClassKey]++);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($getCnt035 > 0) {
                $where  = " WHERE ENTEXAMYEAR  = '{$model->ObjYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";
                $where .= "   AND SEQ          = '035' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
