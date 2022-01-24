<?php

require_once('for_php7.php');

class knjz290_2SubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjz290_2index.php", "", "subform1");
        $arg["reload"] = "";

        //警告メッセージを表示しない場合 *確認する
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if (!isset($model->warning) && isset($model->staffcd) && isset($model->classcd) && isset($model->school_kind) && isset($model->sdate))
            {   
                $Row = knjz290_2Query::getRow2($model->staffcd,$model->classcd,$model->sdate,$model);
            } else {
                $Row =& $model->field;
                $Row["SDATE"] = $model->field["SDATE"];
                $Row["EDATE"] = $model->field["EDATE"];
            }
        } else {
            if (!isset($model->warning) && isset($model->staffcd) && isset($model->classcd) && isset($model->sdate))
            {   
                $Row = knjz290_2Query::getRow2($model->staffcd,$model->classcd,$model->sdate,$model);
            } else {
                $Row =& $model->field;
                $Row["SDATE"] = $model->field["SDATE"];
                $Row["EDATE"] = $model->field["EDATE"];
            }
        }

        //DB接続
        $db = Query::dbCheckOut();

        //職員情報を取得
        $info = $db->getRow(knjz290_2Query::getStfInfo2($model->staffcd), DB_FETCHMODE_ASSOC);
        //表示用
        $arg["STAFFINFO"] = $info["STAFFCD"].'　'.$info["STAFFNAME"];

        //フィールド取得用に作成
        knjCreateHidden($objForm, "STAFFCD", $info["STAFFCD"]);

        $arg["STAFFCD"] = $info["STAFFCD"];
        $arg["STAFFNAME"] = $info["STAFFNAME"];
        
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
            $result = $db->query(knjz290_2Query::getClassSdateEdate($model->staffcd, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //教育課程対応
                /*if ($model->Properties["useCurriculumcd"] == '1') {
                    $model->school_kind = $row["SCHOOL_KIND"];
                    $model->curriculum_cd = $row["CURRICULUM_CD"];
                }*/
                $row["SDATE"]     = str_replace("-","/",$row["SDATE"]);
                $row["EDATE"]     = str_replace("-","/",$row["EDATE"]);
                $arg["rereki_data"][]  = $row;
            }
        }

        /********************/
        /* コンボボックス   */
        /********************/
        //教科コンボ
        $query = knjz290_2Query::getName($model);
        $result = $db->query($query);
        $opt_classcd = array();
        $opt_classcd[] = array("label" => '', "value" => '');
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");
                //NO002
                $opt_classcd[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."&nbsp;".$row["CLASSNAME"],
                                       "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
            }
        } else {
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");
                //NO002
                $opt_classcd[] = array("label" => $row["CLASSCD"]."&nbsp;".$row["CLASSNAME"],
                                       "value" => $row["CLASSCD"]);
            }
        }
        $result->free();
        $extra = "";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["data"]["CLASSCD"] = $this->createCombo($objForm, "CLASSCD", $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"], $opt_classcd, $extra, 1);
        } else {
            $arg["data"]["CLASSCD"] = $this->createCombo($objForm, "CLASSCD", $Row["CLASSCD"], $opt_classcd, $extra, 1);
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

        Query::dbCheckIn($db);

        /*if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjz290_2index.php?cmd=list2';";
        }*/

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290_2SubForm1.html", $arg);
    }
    
    //コンボ作成
    function createCombo(&$objForm, $name, $value, $options, $extra, $size)
    {
        $objForm->ae( array("type"      => "select",
                            "name"      => $name,
                            "size"      => $size,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "options"   => $options));
        return $objForm->ge($name);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
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
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectSubclass");
    knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

    
}
?>
