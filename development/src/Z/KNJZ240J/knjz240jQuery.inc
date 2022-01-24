<?php

require_once('for_php7.php');
class knjz240jQuery extends Query
{
    //文書種類を取得
    public function getDocumentcd($model)
    {
        if ($model->field["DOCUMENT_DIV"] == '1') {
            if ($model->schoolName == 'sakae') {
                $setArr = array("4"  => "健康診断結果のお知らせ",
                                "5"  => "受診報告書",
                                "6"  => "検診のおすすめ票（眼科）",
                                "7"  => "検診のおすすめ票（歯科）",
                                "8"  => "検診のおすすめ票（耳鼻科）",
                                "9"  => "検診のおすすめ票（心臓）",
                                "10" => "検診のおすすめ票（内科）",
                                "11" => "検診のおすすめ票（尿）",
                                "12" => "心臓検診結果のお知らせ",
                                "13" => "腎臓検診第2次（尿検査）のお知らせ",
                                "14" => "尿検査の結果について ",
                                "15" => "治療継続証明書",
                                "16" => "健康診断結果のお知らせ",
                                "17" => "受診報告書",
                                "18" => "検診のおすすめ票（眼科）",
                                "19" => "検診のおすすめ票（歯科）",
                                "20" => "検診のおすすめ票（耳鼻科）",
                                "21" => "検診のおすすめ票（心臓）",
                                "22" => "検診のおすすめ票（内科）",
                                "23" => "検診のおすすめ票（尿）",
                                "24" => "検診のおすすめ票（結核）",
                                "25" => "心臓検診結果のお知らせ",
                                "26" => "腎臓検診第2次（尿検査）のお知らせ",
                                "27" => "尿検査の結果について",
                                "28" => "結核検診の結果について",
                                "29" => "治療継続証明書");

                $query  = "";
                $union  = "";
                foreach ($setArr as $key => $titleName) {
                    $query .= " {$union} ";
                    $query .= " SELECT ";
                    $query .= "     '".sprintf("%02d", $key)."' AS VALUE, ";
                    $query .= "     '{$key}：{$titleName}' AS LABEL ";
                    $query .= " FROM ";
                    $query .= "     SYSIBM.SYSDUMMY1 ";
                    $union  = " UNION ";
                }
            } elseif ($model->documentMstDocumentCdFlg == '2') {
                $query  = " SELECT ";
                $query .= "     '01' AS VALUE, ";
                $query .= "     '1：健康診断結果通知書（一覧）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '02' AS VALUE, ";
                $query .= "     '2：健康診断結果通知書（運動器）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '03' AS VALUE, ";
                $query .= "     '3：健康診断結果通知書（眼科）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '04' AS VALUE, ";
                $query .= "     '4：健康診断結果通知書（結核）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '05' AS VALUE, ";
                $query .= "     '5：健康診断結果通知書（視力）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '06' AS VALUE, ";
                $query .= "     '6：健康診断結果通知書（歯科）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '07' AS VALUE, ";
                $query .= "     '7：健康診断結果通知書（耳鼻科）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '08' AS VALUE, ";
                $query .= "     '8：健康診断結果通知書（色覚）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '09' AS VALUE, ";
                $query .= "     '9：健康診断結果通知書（心電図）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '10' AS VALUE, ";
                $query .= "     '10：健康診断結果通知書（聴力）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '11' AS VALUE, ";
                $query .= "     '11：健康診断結果通知書（内科）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '12' AS VALUE, ";
                $query .= "     '12：健康診断結果通知書（尿）' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
            } else {
                $query  = " SELECT ";
                $query .= "     '01' AS VALUE, ";
                $query .= "     '1：定期健康診断のお知らせ' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '02' AS VALUE, ";
                $query .= "     '2：眼科受診のお知らせ' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '03' AS VALUE, ";
                $query .= "     '3：健康診断の未受検項目のある生徒へ' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     '04' AS VALUE, ";
                $query .= "     '4：検診結果のお知らせ' AS LABEL ";
                $query .= " FROM ";
                $query .= "     SYSIBM.SYSDUMMY1 ";
                if ($model->documentMstDocumentCdFlg == '1') {
                    $query .= " UNION ";
                    $query .= " SELECT ";
                    $query .= "     '05' AS VALUE, ";
                    $query .= "     '5：眼科検診のお知らせ' AS LABEL ";
                    $query .= " FROM ";
                    $query .= "     SYSIBM.SYSDUMMY1 ";
                    $query .= " UNION ";
                    $query .= " SELECT ";
                    $query .= "     '06' AS VALUE, ";
                    $query .= "     '6：検診結果のお知らせ(歯・口腔)1' AS LABEL ";
                    $query .= " FROM ";
                    $query .= "     SYSIBM.SYSDUMMY1 ";
                    $query .= " UNION ";
                    $query .= " SELECT ";
                    $query .= "     '07' AS VALUE, ";
                    $query .= "     '7：検診結果のお知らせ(歯・口腔)2' AS LABEL ";
                    $query .= " FROM ";
                    $query .= "     SYSIBM.SYSDUMMY1 ";
                    $query .= " UNION ";
                    $query .= " SELECT ";
                    $query .= "     '08' AS VALUE, ";
                    $query .= "     '8：定期健康診断の結果について' AS LABEL ";
                    $query .= " FROM ";
                    $query .= "     SYSIBM.SYSDUMMY1 ";
                }
            }
        } elseif ($model->field["DOCUMENT_DIV"] == '4') {
            $query  = " SELECT ";
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     NAME_MST ";
            $query .= " WHERE ";
            $query .= "     NAMECD1 = 'Z057' ";
        } else {
            $query  = " SELECT ";
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     NAME_MST ";
            $query .= " WHERE ";
            $query .= "     NAMECD1 = 'Z049' ";
            $query .= "     AND NAMESPARE1 = '{$model->field["DOCUMENT_DIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    public function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM DOCUMENT_MST WHERE DOCUMENTCD ='".$model->field["DOCUMENTCD"]."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //追加データ取得
    public function getList()
    {
        $query  = "SELECT * FROM DOCUMENT_DETAIL_MST";
        return $query;
    }

    //INSERT
    public function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["DOCUMENTCD"][TEXT]       = $fields["DOCUMENTCD"];
        $data["TITLE"][TEXT]            = $fields["TITLE"];
        if ($model->schoolName == 'sakae') {
            $data["CERTIF_NO"][TEXT]        = $fields["CERTIF_NO"];
        }
        $data["TEXT"][TEXT]             = $fields["TEXT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "DOCUMENT_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= " DOCUMENT_MST ";
        $query .= " WHERE ";
        $query .= " DOCUMENTCD = '".$fields["DOCUMENTCD"] ."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
    //REPLACE
    public function &getReplaceQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= " DOCUMENT_DETAIL_MST ";

        $db->query($query);

        if (isset($fields['PATTERN']) && $fields['PATTERN']>1) {
            for ($i = 0; $i < $fields['PATTERN'] - 1; $i++) {
                $data = array();
                $data["DOCUMENTCD"][TEXT] = '01';
                $data["SEQ"][TEXT]        = $i + 2;
                $data["TITLE"][TEXT]      = '';
                $data["TEXT"][TEXT]       = $fields['LIST']["TEXT_" . $i];
                $data["FOOTNOTE"][TEXT]   = '';
                $data["UPDATED"][NUMBER]  = "sysdate()";
                $query = Query::insertSQL($data, "DOCUMENT_DETAIL_MST");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
        return;
    }

    public function isKumamoto()
    {
        $db = Query::dbCheckOut();
        $query = "select NAME1 from NAME_MST where NAMECD1 = 'Z010' and NAMECD2 = '00'";
        $name1 = $db->getOne($query);
        Query::dbCheckIn($db);
        return $name1 == 'kumamoto';
    }

    //Z010取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }
}
