<?php

require_once('for_php7.php');

class knjz175Form1
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz175index.php", "", "edit");

        //年度を表示
        $arg["header"] = CTRL_YEAR;

        //コピーボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "前年度からコピー",
                            "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        $db = Query::dbCheckOut();

        //リスト表示
        $cd = $row2 = array();  //$rowはデータベース参照用、$row2は画面表示用
        $query  = knjz175Query::Listdata($model);
        $result = $db->query($query);
        for($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++)
        {
            if($i==0) $cd[] = $row["PGROUPCD"];

            //コードを比較
            if (in_array($row["PGROUPCD"], $cd)) {

                $row2["PGROUPCD"] = View::alink("knjz175index.php", $row["PGROUPCD"],"target=right_frame",
                                            array("PGROUPCD"    => $row["PGROUPCD"],
                                                  "PGROUPNAME"  => $row["PGROUPNAME"],
                                                  "PLESSONCNT"  => $row["PLESSONCNT"],
                                                  "PFRAMECNT"   => $row["PFRAMECNT"],
                                                  "cmd"         => "edit"));
                $row2["PGROUPNAME"]    = $row["PGROUPNAME"];
                $row2["PLESSONCNT"]    = $row["PLESSONCNT"];
                $row2["PFRAMECNT"]     = $row["PFRAMECNT"];
                $row2["CGROUPCDNAME"] .= $row["CGROUPCDNAME"]."<BR>";   //同じ親群のデータ追加
                $row2["CLESSONCNT"]   .= $row["CLESSONCNT"]."<BR>";     //同じ親群のデータ追加
                $row2["CFRAMECNT"]    .= $row["CFRAMECNT"]."<BR>";      //同じ親群のデータ追加

            } else {

                $arg["data"][] = $row2;

                $cd[] = $row["PGROUPCD"];   //コードを保存
                $row2["CGROUPCDNAME"] = ""; //データクリア
                $row2["CLESSONCNT"]   = ""; //データクリア
                $row2["CFRAMECNT"]    = ""; //データクリア

                $row2["PGROUPCD"] = View::alink("knjz175index.php", $row["PGROUPCD"],"target=right_frame",
                                            array("PGROUPCD"    => $row["PGROUPCD"],
                                                  "PGROUPNAME"  => $row["PGROUPNAME"],
                                                  "PLESSONCNT"  => $row["PLESSONCNT"],
                                                  "PFRAMECNT"   => $row["PFRAMECNT"],
                                                  "cmd"         => "edit"));
                $row2["PGROUPNAME"]    = $row["PGROUPNAME"];
                $row2["PLESSONCNT"]    = $row["PLESSONCNT"];
                $row2["PFRAMECNT"]     = $row["PFRAMECNT"];
                $row2["CGROUPCDNAME"] .= $row["CGROUPCDNAME"]."<BR>";   //同じ親群のデータ追加
                $row2["CLESSONCNT"]   .= $row["CLESSONCNT"]."<BR>";     //同じ親群のデータ追加
                $row2["CFRAMECNT"]    .= $row["CFRAMECNT"]."<BR>";      //同じ親群のデータ追加
            }
        }
        $arg["data"][] = $row2;
        $result->free();

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );

        //初期処理
        $cntclass = $db->getOne(knjz175Query::cnt_Electclass(CTRL_YEAR));
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["Closing"] = "closing_window();";  //権限チェック
        }elseif($cntclass == 0){
            $arg["Closing"] = "closing_window(1);"; //利用データチェック
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz175Form1.html", $arg);
        }
    }
?>
