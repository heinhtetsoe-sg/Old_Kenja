<?php

require_once('for_php7.php');

class knjh090form1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh090index.php", "", "edit");

        $db = Query::dbCheckOut();

        //PDF取込
        if ($model->Properties["savePdfFolderH090"]) {
            $arg["PDF"] = '1';
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh090Query::getSchregnoName($model->schregno);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"]     = $Row["NAME_SHOW"];

        //学籍賞罰データよりデータを取得
        if ($model->schregno) {
            $result = $db->query(knjh090Query::getAward($model->schregno, $model->chktokiwagiflg));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DETAIL_DIV_SHOW"] = '1';

                $row["DETAIL_SDATE"] = str_replace("-", "/", $row["DETAIL_SDATE"]);
                $row["DETAIL_EDATE"] = str_replace("-", "/", $row["DETAIL_EDATE"]);
                $row["TMP_PDF"] = getPdfCnt($model, $row);
                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "clear", 0);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"] = "window.open('knjh090index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh090Form1.html", $arg);
    }
}

//PDF件数
function getPdfCnt($model, $row)
{
    $cnt = 0;
    //移動後のファイルパス単位
    if ($model->schregno) {
        $dir = "/pdf/{$model->schregno}/{$model->Properties["savePdfFolderH090"]}/";
        $dataDir = DOCUMENTROOT . $dir;
        $searchFileName = $row["YEAR"].$row["DETAIL_DIV"].str_replace("/", "", str_replace("-", "", $row["DETAIL_SDATE"]));
        if (!is_dir($dataDir)) {
            //echo "ディレクトリがありません。";
        } elseif ($aa = opendir($dataDir)) {
            while (false !== ($filename = readdir($aa))) {
                $filedir = REQUESTROOT . $dir . $filename;
                $info = pathinfo($filedir);
                //拡張子
                if ($info["extension"] == "pdf" && preg_match("/".$searchFileName."/", $info["basename"]) && $cnt < 5) {
                    $cnt++;
                }
            }
            closedir($aa);
        }
    }
    return $cnt > 0 ? "有" : "";
}
