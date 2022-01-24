<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja120aSubForm4
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform4", "POST", "knja120aindex.php", "", "subform4");

        $arg["NAME_SHOW"] = $model->schregno."　：　".$model->name;

        $db = Query::dbCheckOut();

        //中高一貫校
        $JH = (get_count($db->getOne(knja120aQuery::getJH())) > 0) ? "1" : "";

        //SQL文発行
        //年度（年次）取得
        $query = knja120aQuery::selectQueryAnnual($model, $JH);
        $result = $db->query($query);
        $opt = array();
        $opt[] = array("label" => "------ すべて表示 ------",
                       "value" => "0,0"
                      );
        if (!isset($model->annual["YEAR"])){
            $model->annual["YEAR"]     = 0;
            $model->annual["ANNUAL"]   = 0;
        }
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int)$row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('subform4');\"",
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
        
        $query = knja120aQuery::selectQuerySubForm4($model, $JH);
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
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
           $data = array( $row["ANNUAL"],
                          $row["CLASSNAME"],
                          //$row["SUBCLASSNAME"],
                          substr($row["SUBCLASSNAME"],0,24),   //2004-06-03 y.arakaki 8文字表示へ変更
                          $row["VALUATION"],
                          $row["GET_CREDIT"],
                          $row["ADD_CREDIT"],
                          (int)$row["GET_CREDIT"]+(int)$row["ADD_CREDIT"],
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
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));


        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                                                
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja120aSubForm4.html", $arg);
    }
}
?>