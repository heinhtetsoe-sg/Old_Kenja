<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010dSubFormSeiseki {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("formSeiseki", "POST", "knje010dindex.php", "", "formSeiseki");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        //年度（年次）取得
        if ($model->cmd == "formSeiseki_first") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = 0;   // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = 0;
        }
        $opt = array();
        $opt[] = array("label" => "------ すべて表示 ------",
                       "value" => "0,0"
                      );
        $query = knje010dQuery::selectQueryAnnual($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );

            if ($model->cmd == "formSeiseki_first" && $model->exp_year == $row["YEAR"]){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('formSeiseki');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

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

        $query = knje010dQuery::selectQueryForm3($model);
        $result = $db->query($query);
        $option  = array("align=\"center\"",
                         "align=\"left\"",
                         "align=\"left\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"right\"",
                         "align=\"left\""
                        );
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
           $data = array( $row["ANNUAL"],
                          $row["CLASSNAME"],
                          substr($row["SUBCLASSNAME"],0,24),
                          $row["VALUATION"],
                          $row["GET_CREDIT"],
                          $row["ADD_CREDIT"],
                          (int)$row["GET_CREDIT"]+(int)$row["ADD_CREDIT"],
                          $row["REMARK"]
                          );

            $t->addData($data, $option);
        }

        $arg["table"] = $t->toTable();

        /**********/
        /* ボタン */
        /**********/
        //戻る
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010dSubFormSeiseki.html", $arg);
    }
}
?>