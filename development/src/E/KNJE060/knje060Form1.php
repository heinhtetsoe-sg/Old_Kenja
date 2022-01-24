<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje060Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;
        $db = Query::dbCheckOut();

        //学年取得
        $query = knje060Query::selectQueryAnnual($model);
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => (int) $row["ANNUAL"] ."学年",
                           "value" => $row["ANNUAL"]
                           );
            if (!isset($model->annual)) $model->annual = $row["ANNUAL"];
        }
        //集計日付の抽出
        $row = $db->getRow(knje060Query::selectQuery($model));
        $model->data["SUMDATE1"] = $row[0];
        $model->data["SUMDATE2"] = $row[1];
        if ($model->data["SUMDATE1"] == ""){
            if (CTRL_SEMESTER == 1){
               $model->data["SUMDATE1"] = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
            }else{
               $model->data["SUMDATE1"] = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER-1]);
            }
        }
        Query::dbCheckIn($db);

        $disabled = '';

        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual,
//                            "extrahtml"  => "onChange=\"return btn_submit('main');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        //評価が１の場合２に置き換える
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "REPLACE",
                            "value"      => "1",
                            "checked"    => ($model->field["REPLACE"] == 1)? true : false,
                            "extrahtml"  => "id=\"REPLACE\""
                             ));

        $arg["REPLACE"] = $objForm->ge("REPLACE");

        //ファイルからの取り込み
        $objForm->ae(array("type"      => "file",
                            "name"      => "FILE",
                            "size"      => 1024000,
                            "extrahtml"   => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //CSV取込みボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "$disabled onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //ラジオボタン
        $arg["RADIO"] = $model->field;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "./knje060index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje060Form1.html", $arg);
    }
}
?>
