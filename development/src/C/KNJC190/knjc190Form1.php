<?php

require_once('for_php7.php');

/********************************************************************/
/* 出欠未入力講座チェックリスト                     山城 2005/10/20 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：科目と教員をラジオで切替える、ソート無   山城 2005/10/22 */
/********************************************************************/

class knjc190Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc190Form1", "POST", "knjc190index.php", "", "knjc190Form1");
        
        //今学期
        knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);

        //学籍処理日
        knjCreateHidden($objForm, "CTRL_DAY", CTRL_DATE);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //切替ラジオ（1:会場,2:受験番号）
        $opt[0]=1;
        $opt[1]=2;

        if (!$model->output) $model->output = 1;
        for ($i = 1; $i <= 2; $i++) {
            $name = "OUTPUT".$i;
            $objForm->ae( array("type"      => "radio",
                                "name"      => "OUTPUT",
                                "value"     => $model->output,
                                "extrahtml" => "onclick =\" return btn_submit('knjc190');\" id=\"$name\"",
                                "multiple"  => $opt));

            $arg["data"][$name] = $objForm->ge("OUTPUT",$i);
        }

        if ($model->output == 1) $arg["subno"] = $model->output;
        if ($model->output == 2) $arg["stfno"] = $model->output;

        /*------------*/
        /* 一覧リスト */
        /*------------*/
        //NO001
        $row1 = array();
        $db = Query::dbCheckOut();

        if ($model->output == 1) $query = knjc190Query::GetSubclass($model);
        if ($model->output == 2) $query = knjc190Query::GetStaff();
        
        $result = $db->query($query);
        if ($model->Properties["notShowStaffcd"] === '1' && $model->output == 2) {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row1[] = array('label' => $row["NAME"],
                                'value' => $row["CD"]);
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row1[] = array('label' => $row["CD"]."　".$row["NAME"],
                                'value' => $row["CD"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);
        
        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASS_NAME",
                            "extrahtml" => "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left','up')\"",
                            "size"      => "20",
                            "options"   => isset($row1)?$row1:array()));

        $arg["data"]["SUBCLASS_NAME"] = $objForm->ge("SUBCLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae( array("type"      => "select",
                            "name"      => "SUBCLASS_SELECTED",
                            "extrahtml" => "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right','up')\"",
                            "size"      => "20",
                            "options"   => array()));

        $arg["data"]["SUBCLASS_SELECTED"] = $objForm->ge("SUBCLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $extra =  "style=\"height:20px;width:40px\" onclick=\"moves('right','up');\"";
        $arg["button"]["subclass_rights"] = knjCreateBtn($objForm, "subclass_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra =  "style=\"height:20px;width:40px\" onclick=\"moves('left','up');\"";
        $arg["button"]["subclass_lefts"] = knjCreateBtn($objForm, "subclass_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra =  "style=\"height:20px;width:40px\" onclick=\"move1('right','up');\"";
        $arg["button"]["subclass_right1"] = knjCreateBtn($objForm, "subclass_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra =  "style=\"height:20px;width:40px\" onclick=\"move1('left','up');\"";
        $arg["button"]["subclass_left1"] = knjCreateBtn($objForm, "subclass_left1", "＜", $extra);

        //印刷対象日付1
        if ($model->field["DATE1"] == "") $model->field["DATE1"] = str_replace("-","/",$model->control["学期開始日付"][CTRL_SEMESTER]);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm    ,"DATE1"    ,$model->field["DATE1"]);

        //印刷対象日付2
        if ($model->field["DATE2"] == "") $model->field["DATE2"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm    ,"DATE2"    ,$model->field["DATE2"]);
        
        //印刷ボタンを作成する
        $extra =  "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //終了ボタンを作成する
        $extra =  "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        
        //学期の開始日と終了日のセット
        if ($model->control["学期数"] == "3") {
            $GAKKISDATE = $model->control["学期開始日付"][1];
            $GAKKIEDATE = $model->control["学期終了日付"][3];
        } else {
            $GAKKISDATE = $model->control["学期開始日付"][1];
            $GAKKIEDATE = $model->control["学期終了日付"][2];
        }
        
        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC190");
        knjCreateHidden($objForm, "GAKKISDATE", $GAKKISDATE);
        knjCreateHidden($objForm, "GAKKIEDATE", $GAKKIEDATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "notShowStaffcd", $model->Properties["notShowStaffcd"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc190Form1.html", $arg); 
    }
}
?>
