<?php

require_once('for_php7.php');

class knje701Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knje701index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);

        //テスト区分・実施日付・テスト名称表示
        $heigan = array();
        $gakka  = array();
        $query = knje701Query::getHeiganList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $heiganCd = $row["HEIGAN_CD"] ."-". $row["HEIGAN_GROUPNAME"] ."-". $row["FACULITYCD"];
            if ($beforeHeiganCd        == $row["HEIGAN_CD"] &&
                $beforeHeiganGroupName == $row["HEIGAN_GROUPNAME"]) {
                //併願コード・併願グループ名称が同じとき
                $heigan[$heiganCd] .= "<br>".$row["DEPARTMENTNAME"];
                $gakka[$heiganCd]  .= "-".$row["DEPARTMENTCD"];
            } else {
                $heigan[$heiganCd] = $row["DEPARTMENTNAME"];
                $gakka[$heiganCd]  = $row["DEPARTMENTCD"];
            }
            //一つ前のデータを保持
            $beforeHeiganCd        = $row["HEIGAN_CD"];
            $beforeHeiganGroupName = $row["HEIGAN_GROUPNAME"];
        }

        if (isset($heigan)) {
            //表示用データを格納
            foreach ($heigan as $key => $val) {
                $heiganDate = explode("-", $key);
                $date["HEIGAN_CD"]        = $heiganDate[0];
                $date["HEIGAN_GROUPNAME"] = $heiganDate[1];
                $date["FACULITYCD"]       = $heiganDate[2];
                $date["DEPARTMENTCD"]     = $gakka[$key];
                $date["DEPARTMENTNAME"]   = $val;
                $arg["data"][] = $date;
            }
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje701Form1.html", $arg);
    }
}
