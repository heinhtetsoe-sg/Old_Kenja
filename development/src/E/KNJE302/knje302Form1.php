<?php

require_once('for_php7.php');

class knje302Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knje302index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "read") {
            unset($model->field);
        }
        if ($model->cmd == "" || $model->cmd == "search") {
            $model->editPreischoolCd = '';
            $model->editVisitDate = '';
            $model->editSeq = '';
            unset($model->field);
        }

        //塾
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOLCD_ID\" onkeydown=\"\"";
        $arg["data"]["PRISCHOOLCD_ID"] = knjCreateTextBox($objForm, $model->preischoolcd, "PRISCHOOLCD_ID", 7, 7, $extra);
        //教室コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOL_CLASS_CD_ID\" onkeydown=\"\"";
        $arg["data"]["PRISCHOOL_CLASS_CD_ID"] = knjCreateTextBox($objForm, $model->preischoolClassCd, "PRISCHOOL_CLASS_CD_ID", 7, 7, $extra);
        //確定
        $extra = "style=\"width:70px\" onclick=\"btn_submit('search');\"";
        $arg["data"]["btn_pri_cd_submit"] = knjCreateBtn($objForm, "btn_pri_cd_submit", "確 定", $extra);
        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD_ID&priname=label_priName&priclasscd=PRISCHOOL_CLASS_CD_ID&priclassname=label_priClassName&priaddr=&submitFlg=1&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 20 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["data"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);

        //塾名
        $query = knje302Query::getPriSchoolName($model);
        $arg["data"]["PRISCHOOL_NAME"] = $db->getOne($query);
        //教室名
        $query = knje302Query::getPriSchoolClassName($model);
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $db->getOne($query);

        // 塾訪問一覧
        $query = knje302Query::selectQuery($model);
        $searchCnt = get_count($db->getCol($query));
        $arg["SEARCH_CNT"] = $searchCnt;
        $arg["SEARCH_CNT_MSG"] = ($searchCnt == 0) ? "該当なし" : "";

        //検索結果
        $checkCnt = 500;
        if($searchCnt > $checkCnt) {
            $model->setWarning("検索結果：".$searchCnt."件です。\\n表示可能件数は".$checkCnt."件までです。");
        } else {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->cmd == "edit"){
                    if ($model->editPreischoolCd == $row["PRISCHOOLCD"] &&
                        $model->editVisitDate == str_replace("-","/", $row["VISIT_DATE"]) &&
                        $model->editSeq == $row["SEQ"]){
                            $model->field["PRISCHOOLCD"] = $row["PRISCHOOLCD"];
                            $model->field["VISIT_DATE"] = $row["VISIT_DATE"];
                            $model->field["SEQ"] = $row["SEQ"];
                            $model->field["PRISCHOOL_CLASS_CD"] = $row["PRISCHOOL_CLASS_CD"];
                            $model->field["PRISCHOOL_NAME"] = $row["PRISCHOOL_NAME"];
                            $model->field["STAFFCD"] = $row["STAFFCD"];
                            $model->field["PRISCHOOL_STAFF"] = $row["PRISCHOOL_STAFF"];
                            $model->field["COMMENT"] = $row["COMMENT"];
                            $model->field["EXAM_STD_INFO"] = $row["EXAM_STD_INFO"];
                            $model->field["REMARK"] = $row["REMARK"];
                            $model->field["UPDATED"] = $row["UPDATED"];
                    }
                }
                //リンク設定
                $extra = "link_submit('".$row["PRISCHOOLCD"]."','".str_replace("-","/", $row["VISIT_DATE"])."','".$row["SEQ"]."')";
                $row["VISIT_DATE"] = View::alink("#", htmlspecialchars($row["VISIT_DATE"]), "onclick=\"$extra\"");
                $arg["data2"][] = $row;
            }
            $result->free();
        }

        // 訪問日付
        $model->visitDate = $model->field['VISIT_DATE'] ? $model->field['VISIT_DATE'] : CTRL_DATE;
        $param = "extra=dateChange(f.document.forms[0][\\'VISIT_DATE\\'].value);";
        $arg["data3"]["VISIT_DATE"] = View::popUpCalendar($objForm, "VISIT_DATE", str_replace("-", "/", $model->visitDate), $param);
        // SEQ
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "SEQ",
            "value" => $model->field['SEQ']
        ));
        $arg["data3"]["SEQ"] = $objForm->ge("SEQ");
        // 職員コンボ
        $query = knje302Query::getStaffList($model);
        $extra = "";
        $arg["data3"]['STAFFCD'] = makeCmb($objForm, $arg, $db, $query, 'STAFFCD', $model->field['STAFFCD'], $extra, 1,'BLANK');
        // 教室名コンボ
        $query = knje302Query::getPrischoolClasstList($model);
        $extra = "";
        $arg["data3"]['PRISCHOOL_CLASS_CD'] = makeCmb($objForm, $arg, $db, $query, 'PRISCHOOL_CLASS_CD', $model->field['PRISCHOOL_CLASS_CD'], $extra, 1,'BLANK');
        // 面接者
        $extra = "";
        $arg["data3"]["PRISCHOOL_STAFF"] = knjCreateTextBox($objForm, $model->field['PRISCHOOL_STAFF'], "PRISCHOOL_STAFF", 10, 10, $extra);
        // コメント
        $extra = "";
        $arg["data3"]["COMMENT"] = knjCreateTextBox($objForm, $model->field['COMMENT'], "COMMENT", 20, 20, $extra);
        // 受験者情報
        $extra = "";
        $arg["data3"]["EXAM_STD_INFO"] = knjCreateTextBox($objForm, $model->field['EXAM_STD_INFO'], "EXAM_STD_INFO", 10, 10, $extra);
        // 備考
        $extra = "";
        $arg["data3"]["REMARK"] = knjCreateTextBox($objForm, $model->field['REMARK'], "REMARK", 10, 10, $extra);
        // 更新日時
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "UPDATED",
            "value" => $model->field['UPDATED']
        ));
        $arg["data3"]["UPDATED"] = $objForm->ge("UPDATED");

        //新規ボタンを作成する
        $objForm->ae( array("type"        => "button",
            "name"        => "btn_new",
            "value"       => "新規",
            "extrahtml"   => "onclick=\"return btn_submit('insert');\"" ) );
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
            "name"        => "btn_update",
            "value"       => "更新",
            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
            "name"        => "btn_delete",
            "value"       => "削除",
            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
            "name"        => "btn_clear",
            "value"       => "取消",
            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
            "name"        => "btn_end",
            "value"       => "終了",
            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"] = array(
            "BTN_NEW"    =>$objForm->ge("btn_new"),
            "BTN_UPDATE" =>$objForm->ge("btn_update"),
            "BTN_DELETE" =>$objForm->ge("btn_delete"),
            "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
            "BTN_END"    =>$objForm->ge("btn_end")
        );

        // hiddenを作成する
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "cmd"
        ));
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "EDIT_PRISCHOOLCD",
            "value" => $model->editPreischoolCd
        ));
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "EDIT_VISIT_DATE",
            "value" => $model->editVisitDate
        ));
        $objForm->ae(array(
            "type" => "hidden",
            "name" => "EDIT_SEQ",
            "value" => $model->editSeq
        ));

        $arg['year'] = $model->year;
        $arg["TITLE"]   = "塾訪問記録登録";

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje302Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $result->free();
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}
?>
