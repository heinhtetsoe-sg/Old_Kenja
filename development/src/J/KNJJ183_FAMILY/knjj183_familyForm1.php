<?php

require_once('for_php7.php');

class knjj183_familyForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjj183_familyForm1.php", "", "edit");
        $arg["data"]  = array();

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knjj183_familyQuery::getSchregno_name($model),DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //家族番号
        $query = knjj183_familyQuery::getFamilyNo($model);
        $model->familyNo = $db->getOne($query);

        //学籍住所データよりデータを取得
        if ($model->schregno) {
            $relaName = array();
            $result = $db->query(knjj183_familyQuery::get_name_mst());
            while( $rowRela = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $relaName[$rowRela["NAMECD1"]][$rowRela["NAMECD2"]] = $rowRela["NAME1"];
            }

            //兄弟姉妹の卒業区分取得
            $GrdBro = array();
            $result = $db->query(knjj183_familyQuery::getGrdBrother($model));
            while ($rowBro = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $GrdBro[$rowBro["SCHREGNO"]] = '('.$rowBro["NAME1"].')';
            }

            //在卒区分（表示用）
            $regd_grd_array = array("1" => "在学", "2" => "卒業");

            //学年表示取得
            $relaG = array();
            $result = $db->query(knjj183_familyQuery::getSchregRegdGdat($model));
            while ($rowG = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $relaG[$rowG["VALUE"]] = $rowG["LABEL"];
            }

            //緊急連絡先情報の取得(2005/10/20 ADD)
            $result = $db->query(knjj183_familyQuery::getAddress_all($model));

            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row  = array_merge((array)$row1,(array)$row2);

                $row["RELA_SEX"]            = $relaName['Z002'][$row["RELA_SEX"]];
                $row["RELA_RELATIONSHIP"]   = $relaName['H201'][$row["RELA_RELATIONSHIP"]];
                $row["RELA_REGIDENTIALCD"]  = $relaName['H200'][$row["RELA_REGIDENTIALCD"]];
                $row["REGD_GRD_FLG"]        = $regd_grd_array[$row["REGD_GRD_FLG"]];
                $row["RELA_GRADE"]          = $relaG[$row["RELA_GRADE"]];
                $row["RELA_BIRTHDAY"]       = str_replace("-","/",$row["RELA_BIRTHDAY"]);
                $row["RELA_SCHREGNO"]       = $row["RELA_SCHREGNO"].$GrdBro[$row["RELA_SCHREGNO"]];

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", 0);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj183_familyForm1.html", $arg);
    }
}
?>
