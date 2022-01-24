<?php

require_once('for_php7.php');

class knjz291SubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjz291index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合 *確認する
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (!isset($model->warning) && isset($model->staffcd) && isset($model->classcd) && isset($model->school_kind) && isset($model->sdate)) {
                $query = knjz291Query::getRow2($model->staffcd, $model->classcd, $model->sdate, $model);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->subField2;
            }
        } else {
            if (!isset($model->warning) && isset($model->staffcd) && isset($model->classcd) && isset($model->sdate)) {
                $query = knjz291Query::getRow2($model->staffcd, $model->classcd, $model->sdate, $model);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->subField2;
            }
        }

        //職員情報
        $info = $db->getRow(knjz291Query::getStfInfo2($model->staffcd), DB_FETCHMODE_ASSOC);
        $arg["STAFFINFO"] = $info["STAFFCD"].'　'.$info["STAFFNAME"];

        //フィールド取得用に作成
        knjCreateHidden($objForm, "STAFFCD", $info["STAFFCD"]);

        $arg["STAFFCD"]     = $info["STAFFCD"];
        $arg["STAFFNAME"]   = $info["STAFFNAME"];
        
        //STAFF_CLASS_MSTよりデータを取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //画面上の表示用
            $arg["CURRICULUM_CD"] = "1";
        } else {
            //画面上の表示用
            $arg["NO_CURRICULUM_CD"] = "1";
        }
        if($model->staffcd) {
            $result = $db->query(knjz291Query::getClassSdateEdate($model->staffcd, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["SDATE"]     = str_replace("-","/",$row["SDATE"]);
                $row["EDATE"]     = str_replace("-","/",$row["EDATE"]);
                $arg["rereki_data"][]  = $row;
            }
        }

        /******************/
        /* コンボボックス */
        /******************/
        //教科コンボ
        $query = knjz291Query::getName($model);
        $result = $db->query($query);
        $opt_classcd = array();
        $opt_classcd[] = array("label" => '', "value" => '');
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $opt_classcd[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."&nbsp;".$row["CLASSNAME"],
                                       "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
            }
        } else {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $opt_classcd[] = array("label" => $row["CLASSCD"]."&nbsp;".$row["CLASSNAME"],
                                       "value" => $row["CLASSCD"]);
            }
        }
        $result->free();
        $extra = "";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"], $opt_classcd, $extra, 1);
        } else {
            $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $Row["CLASSCD"], $opt_classcd, $extra, 1);
        }

        /********************/
        /* テキストボックス */
        /********************/
        //開始日付
        $arg["data"]["SDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "SDATE", str_replace("-","/",$Row["SDATE"]), ""));

        //終了日付
        $arg["data"]["EDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "EDATE", str_replace("-","/",$Row["EDATE"]), ""));

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz291SubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('subform1_add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('subform1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('subform1_delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('subform1_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectSubclass");
    knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);
}
?>
