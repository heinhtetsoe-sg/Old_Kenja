<?php

require_once('for_php7.php');


class knjp182kModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt = array(); // セレクトデータ
    var $selectleft;

    function knjp182kModel() {
    }

    function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         // 学期コード
                             "UPDATE_DIV"       =>  VARS::post("UPDATE_DIV"),       // 更新区分
                             "KASAN_MONTH"      =>  VARS::post("KASAN_MONTH"),      // 指定月
                             "KUBUN"            =>  VARS::post("KUBUN"),            // 印刷指定
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    // 年組
                     );
        list($ymd, $kasanDiv) = preg_split("/:/", $this->field["KASAN_MONTH"]);
        $this->field["KASAN_DIV"] = $kasanDiv;
        $this->field["KASAN_YMD"] = $ymd;

        $this->cmd = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class' && $this->cmd != 'csv') $this->select_opt = array();
    }

    // 履修科目データＣＳＶ出力
    function getDownloadModel()
    {
        $header = array("GRADE"     => "年",
                        "HR_CLASS"  => "組",
                        "ATTENDNO"  => "番号",
                        "SCHREGNO"  => "学籍番号",
                        "NAME"      => "生徒氏名",
                        "PASSNO"    => "認定番号",
                        "S_YM"      => "開始年月",
                        "ADDMONEY"  => "加算額",
                        "INCOME"    => "所得額");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        //生徒単位の配列にする
        $arr_selectleft = explode(",", $this->selectleft);
        $arr_schregNo = array();
        if ($this->field["KUBUN"] == 1) {
            foreach ($arr_selectleft as $key => $val) {
                $studentQuery = knjp182kQuery::getSchno($this, $val);
                $result = $db->query($studentQuery);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $arr_schregNo[] = $row["SCHREGNO"];
                }
            }
        } else {
            foreach ($arr_selectleft as $key => $val) {
                $tmp_selectleft = explode("-", $val);
                $arr_schregNo[] = $tmp_selectleft[0];
            }
        }

        //データを更新する。
        if ($this->field["UPDATE_DIV"] == 1) {
            $query = knjp182kQuery::deleteSql($this);
            $db->query($query);
        }

        foreach ($arr_schregNo as $key => $val) {
            if ($this->field["UPDATE_DIV"] == 2) {
                $query = knjp182kQuery::deleteSql($this, $val);
                $db->query($query);
            }
            //データ作成
            $passNo = $db->getOne(knjp182kQuery::getPassNo($val));
            $query = knjp182kQuery::reductionAuthorizeDatIns($val, $this, $passNo);
            $db->query($query);

            $query = knjp182kQuery::selectCsvData($this, $val);
            $csvRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $csvRow["S_YM"] = $this->field["KASAN_YMD"];
            $csvRow = str_replace(",", ".", $csvRow);
            $contents .= implode($csvRow, ",") ."\n";
            $header_data = true;
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if($header_data)
        {
            $filename = sprintf("%s.csv","受給資格認定申請者入力対応用");
            common::downloadContents($contents, $filename);
            return true;
        }else{
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

}
?>
