<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_tuuchisyoken_selectForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_tuuchisyoken_selectindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

		//学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        // ターゲットテキストボックス有無
        $arg['isTarget'] = $model->target ? "1" : "";

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        // 学期末の表示(1:9学期表示)
        $model->endTerm = "";
        // 学期情報取得
        $semesterList = array();
        $query = knjx_tuuchisyoken_selectQuery::getSemesterMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->endTerm != '1' && $row['SEMESTER'] == '9') {
                continue;
            }

            // 学期毎のチェックボックス
            $extra = " checked id=\"CHECK\" onclick=\"return OptionUse(this);\"";
            $extra = " checked onclick=\"return OptionUse(this);\"";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK_SEMESTER", $row['SEMESTER'], $extra, "");
            $semesterList[] = $row;
        }
        $arg["SEMESTER"] = $semesterList;

        // 学校種類(SCHOOL_KIND)取得(呼出元画面から設定されていない場合は生徒から取得する)
        if (!$model->schoolKind) {
            $model->schoolKind = $db->getOne(knjx_tuuchisyoken_selectQuery::getSchoolKind($model));
        }

        // プロパティからデータの取得情報を取得({$model->schoolKind}_tutihyosanshou_MAX_CNT)
        $fieldList = array();
        $dataCnt = 0;
        $propNameMaxCnt = $model->schoolKind."_tutihyosanshou_MAX_CNT";
        if ($model->Properties[$propNameMaxCnt]) {
            $dataCnt = $model->Properties[$propNameMaxCnt];
            // データ件数分プロパティ取得
            for ($i=1; $i <= $dataCnt; $i++) { 
                $propNameItem = $model->schoolKind."_tutihyosanshou:".$i;
                if (!$model->Properties[$propNameItem]) {
                    continue;
                }
                list($tableName, $fieldName, $whereVal, $titleName) = explode("@", $model->Properties[$propNameItem]);
                $field = array("TABLE" => $tableName, "FIELD" => $fieldName, "WHERE" => $whereVal, "TITLE" => $titleName);
                $fieldList[$i] = $field;
            }
        }
        // プロパティからデータの取得情報を取得(HREPORTREMARK_DAT__XXXXXXXX)
        // ※「{$model->schoolKind}_tutihyosanshou_MAX_CNT」のプロパティが存在しない場合のみ
        if (get_count($fieldList) <= 0) {
            $propCnt = 1;
            foreach ($model->Properties as $key => $val) {
                if (strpos($key, 'HREPORTREMARK_DAT__') === false) {
                    continue;
                }
                list($tableName, $filedName) = preg_split("/__/", $key);
                $fieldList[$propCnt] = array("TABLE" => $tableName, "FIELD" => $filedName, "WHERE" => "", "TITLE" => $model->Properties[$key]);
                $propCnt++;
            }
        }
        // プロパティの設定がない場合のデフォルト項目
        if (get_count($fieldList) <= 0) {
            $fieldList[1] = array("TABLE" => "HREPORTREMARK_DETAIL_DAT", "FIELD" => "REMARK1", "WHERE" => "01:01", "TITLE" => "学級活動");
            $fieldList[2] = array("TABLE" => "HREPORTREMARK_DETAIL_DAT", "FIELD" => "REMARK2", "WHERE" => "01:01", "TITLE" => "生徒会活動");
            $fieldList[3] = array("TABLE" => "HREPORTREMARK_DETAIL_DAT", "FIELD" => "REMARK3", "WHERE" => "01:01", "TITLE" => "学校行事");
            $fieldList[4] = array("TABLE" => "HREPORTREMARK_DETAIL_DAT", "FIELD" => "REMARK2", "WHERE" => "02:01", "TITLE" => "部活動の記録");
            $fieldList[5] = array("TABLE" => "HREPORTREMARK_DETAIL_DAT", "FIELD" => "REMARK1", "WHERE" => "03:01", "TITLE" => "その他活動");
        }

        // 項目毎にデータ取得
        foreach ($fieldList as $key => $field) {
            $semesterValue = array();
            $query = "";
            if ($field['TABLE'] == 'HREPORTREMARK_DAT') {
                $query = knjx_tuuchisyoken_selectQuery::getHreportremarkDat($model, $field['FIELD']);
            } else {
                list($div, $code) = explode(':', $field['WHERE']);
                $query = knjx_tuuchisyoken_selectQuery::getHreportremarkDetailDat($model, $field['FIELD'], $div, $code);
            }

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $semesterValue[$row['SEMESTER']] = $row['VALUE'];
            }
            $fieldList[$key]['VALUES'] = $semesterValue;
        }
        // 取得した項目をデータ表示用に作成
        for ($i=1; $i <= get_count($fieldList); $i++) { 
            $field = $fieldList[$i];
            $values = $field['VALUES'];

            $lineData = array();
            $lineData['TITLE'] = $field['TITLE'];

            for ($j=0; $j < get_count($semesterList); $j++) { 
                $semester = $semesterList[$j];
                $value = '';
                if ($values[$semester['SEMESTER']]) {
                    $value = $values[$semester['SEMESTER']];
                }
                // 表示項目のテキストエリア作成
                $extra = "style=\"height:75px; white-space: pre-wrap;\" onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return checkKeyDown(event)\"";
                $lineData['SEMESTER'][]['VALUE'] = knjCreateTextArea($objForm, $semester['SEMESTER'].'_'.$i, 4, 43, "soft", $extra, $value);
            }
            // 行毎のチェックボックス
            $extra = " checked id=\"CHECK\" onclick=\"return OptionUse(this);\"";
            $extra = "onclick=\"OptionUse(this);\"";
            $lineData["CHECK"] = knjCreateCheckBox($objForm, "CHECK_LINE", $i, $extra, "");

            $arg["data"][] = $lineData;
        }

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_tuuchisyoken_selectForm1.html", $arg);
    }

}

?>
