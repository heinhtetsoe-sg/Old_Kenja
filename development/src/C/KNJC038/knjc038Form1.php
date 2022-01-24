<?php

require_once('for_php7.php');

class knjc038Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //DB接続
        $db = Query::dbCheckOut();
        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ取込";
        if (!$model->field["YEAR"]) {
            $model->field["YEAR"] = CTRL_YEAR;
        }

        /****************/
        /* ラジオボタン */
        /****************/
        //範囲選択 1:日・校指定 2:期間範囲
        $model->field["HANI_DIV"] = $model->field["HANI_DIV"] ? $model->field["HANI_DIV"] : '1';
        $opt = array(1, 2);
        $click = "onClick=\"btn_submit('main')\";";
        $extra = array($click." id=\"HANI_DIV1\"", $click." id=\"HANI_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "HANI_DIV", $model->field["HANI_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /**********/
        /* コンボ */
        /**********/
        //年度
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, $model);
        //出欠コード
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KINTAI", $model->field["KINTAI"], $extra, 1, $model);
        //処理名
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新", "value" => "1");
        $opt_shori[] = array("label" => "削除", "value" => "2");
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            if ($model->cmd == "") {
                $check_header = "checked";
            } else {
                $check_header = "";
            }
        }

        $extra = " id=\"HEADER\"";
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "HEADER",
                            "value"     => "on",
                            "extrahtml" =>$check_header.$extra ));
        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        /****************/
        /* ラジオボタン */
        /****************/
        //出力取込種別
        $opt[0]=1; //ヘッダ出力
        $opt[1]=2; //データ取込
        $opt[2]=3; //エラー出力

        if ($model->field["OUTPUT"]=="") {
            $model->field["OUTPUT"] = "1";
        }
        for ($i = 1; $i <= 3; $i++) {
            $name = "OUTPUT".$i;
            $objForm->ae(array("type"       => "radio",
                                "name"      => "OUTPUT",
                                "value"     => $model->field["OUTPUT"],
                                "extrahtml" => "id=\"$name\"",
                                "multiple"  => $opt));

            $arg["data"][$name] = $objForm->ge("OUTPUT", $i);
        }

        /********/
        /* FILE */
        /********/
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc038index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc038Form1.html", $arg);
    }
}
/******************************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
    if ($name == 'KINTAI') {
        if ($model->field["HANI_DIV"] == '1') {
            $kintai = "'1','2','3','6','14'";
        } else {
            $kintai = "'1','2','3','6'";
        }
        $query = knjc038Query::getDiCd($kintai, $model->field["YEAR"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
    } elseif ($name == 'YEAR') {
        $query     = knjc038Query::getThisYear();
        $thisYear  = $db->getOne($query); //コントロールマスタのMAX(CTRL_YEAR)を取得
        $startYear = $thisYear - $model->properties["useAdminYearPast"];   //menuInfo.propertiesの値を引く
        $endYear   = $thisYear + $model->properties["useAdminYearFuture"]; //menuInfo.propertiesの値を足す
        $opt       = array();
        for ($i = $startYear; $i <= $endYear; $i++) {
            $opt[] = array("label" => $i,"value" => $i);
        }
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
