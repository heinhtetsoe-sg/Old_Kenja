<?php

require_once('for_php7.php');

class knja430sForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knja430sindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->staffcd) {
            //職員番号・氏名
            $headerRow = $db->getRow(knja430sQuery::getStaffName($model->staffcd), DB_FETCHMODE_ASSOC);
            $arg["header"]["STAFFCD"] = $headerRow["STAFFCD"];
            $arg["header"]["STAFFNAME"] = $headerRow["STAFFNAME"];

            //リスト
            $result = $db->query(knja430sQuery::getStampList($model->staffcd));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                //レコードを連想配列のまま配列$arg[data]に追加していく。 
                array_walk($row, "htmlspecialchars_array");

                $row["DIST"] = ($row["DIST"] == 1) ? $row["DIST"] : "";
                $row["START_DATE"] = str_replace("-","/",$row["START_DATE"]);
                $row["STOP_DATE"] = str_replace("-","/",$row["STOP_DATE"]);
                $row["DATE"] = str_replace("-","/",$row["DATE"]);
                //リンク
                $row["URL"] = View::alink("knja430sindex.php", $row["STAMP_NO"], "target=edit_frame",
                                            array("cmd"         => "edit",
                                                  "STAMP_NO"    => $row["STAMP_NO"]
                                                  ));
                //印影の表示
                if (strlen($row["DATE"])) {
                    $stampFile = $row["STAMP_NO"].".bmp";
                    $src = REQUESTROOT ."/image/stamp/" .$stampFile;
                    $row["IMAGE"] = '<image src="'.$src.'" alt="'.$stampFile.'" width="60" height="60">';
                } else {
                    $row["IMAGE"] = "未";
                }

                $arg["data"][] = $row;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"] = "parent.edit_frame.location.href='knja430sindex.php?cmd=edit'";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja430sForm1.html", $arg);
    }
} 
?>
