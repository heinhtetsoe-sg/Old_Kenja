<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje050SubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knje050index.php", "", "subform1");

        $arg["NAME_SHOW"] = $model->schregno."　：　".$model->name;

        $db = Query::dbCheckOut();

        //SQL文発行
        //年度（年次）取得
        $query = knje050Query::selectQueryAnnual($model);
        $result = $db->query($query);
        $opt = array();
        $opt[] = array("label" => "------ すべて表示 ------",
                       "value" => "0,0"
                      );
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["ANNUAL_YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["ANNUAL_YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"])){
                 $model->annual["YEAR"]     = $row["ANNUAL_YEAR"];
                 $model->annual["ANNUAL"]   = $row["ANNUAL"];
            }
        }

       $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('subform1');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        //
        $header = array("学年<BR>(年次)",
                        "教科名",
                        "科目名",
#                        "前回<BR>評定",
#                        "今回<BR>評定",
                        "評価",
                        "標準<BR>単位",
                        "増加<BR>単位",
                        "合計",
                        "学習記録<BR>の備考"
                        );

        $width  = array("45",
                        "170",
                        "335",
#                        "40",
#                        "40",
                        "40",
                        "40",
                        "40",
                        "40",
                        "*");

        $t = new Table($header, $width);

        //擬似フレームの高さを設定
        $t->setFrameHeight(300);

        $query = knje050Query::selectQuerySubForm1($model);

        $result = $db->query($query);

        $option  = array("align=\"center\" nowrap",
                         "align=\"left\" nowrap",
                         "align=\"left \" nowrap",
#                         "align=\"center\" nowrap",
#                         "align=\"center\" nowrap",
                         "align=\"center\" nowrap",
                         "align=\"center\" nowrap",
                         "align=\"center\" nowrap",
                         "align=\"center\" nowrap",
                         "align=\"left\" "
                        );

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
           $data = array( $row["ANNUAL"],
                          $row["CLASSCD"]." ".$row["CLASSNAME"],
                          $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
#                          $row["OLD_TMPVAL"],
#                          $row["NEW_TMPVAL"],
                          $row["GRAD_VALUE"],
                          $row["GET_CREDIT"],
                          $row["ADD_CREDIT"],
                          $row["GET_CREDIT"]+$row["ADD_CREDIT"],
                          $row["REMARK"]
                          );


            $t->addData($data, $option);

        }
        Query::dbCheckIn($db);

        $arg["table"] = $t->toTable();


        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻る",
//                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));
                            "extrahtml" => "onclick=\"return closeWin()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                                                
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje050SubForm1.html", $arg);
    }
}
?>