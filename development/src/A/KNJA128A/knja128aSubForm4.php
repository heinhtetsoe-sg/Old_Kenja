<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja128aSubForm4
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knja128aindex.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  ：  ".$model->name;

        //SQL文発行
        //年度（年次）取得
        $query = knja128aQuery::selectQueryAnnual($model);
        $result = $db->query($query);
        $opt = array();
        $opt[] = array("label" => "------ すべて表示 ------", "value" => "0,0");
        if (!isset($model->annual["YEAR"])) {
            $model->annual["YEAR"]     = 0;
            $model->annual["ANNUAL"]   = 0;
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int)$row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
        }
        $extra = "onChange=\"return btn_submit('subform4');\"";
        $arg["ANNUAL"] = knjCreateCombo($objForm, "ANNUAL", $model->annual["YEAR"].",".$model->annual["ANNUAL"], $opt, $extra, 1);

        $header = array("学年(年次)",
                        "教科名",
                        "科目名",
                        "評定",
                        "標準単位",
                        "増加単位",
                        "合計",
                        "学習記録の備考"
                        );

        $width = array("45",
                        "90",
                        "200",
                        "25",
                        "25",
                        "25",
                        "25",
                        "*");

        $t = new Table($header, $width);
        //擬似フレームの高さを設定
        $t->setFrameHeight(300);

        //成績一覧表示
        $query = knja128aQuery::selectQuerySubForm4($model);
        $result = $db->query($query);
        $option  = array("align=\"center\"",
                         "align=\"left\"",
                         "align=\"left\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"center\""
                        );
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array($row["ANNUAL"],
                         $row["CLASSNAME"],
                         substr($row["SUBCLASSNAME"], 0, 24),
                         $row["VALUATION"],
                         $row["GET_CREDIT"],
                         $row["ADD_CREDIT"],
                         (int)$row["GET_CREDIT"]+(int)$row["ADD_CREDIT"],
                         $row["REMARK"]
                        );

            $t->addData($data, $option);
        }

        $arg["table"] = $t->toTable();

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128aSubForm4.html", $arg);
    }
}
