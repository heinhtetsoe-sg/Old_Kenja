<?php

require_once('for_php7.php');


class knjp181kModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $staffcd;
    var $select_opt = array(); // セレクトデータ
    var $selectleft;

    function knjp181kModel() {
    }

    function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         // 学期コード
                             "UPDATE_DIV"       =>  VARS::post("UPDATE_DIV"),       // 更新区分
                             "RENBAN1"          =>  VARS::post("RENBAN1"),          // 新規連番
                             "RENBAN2"          =>  VARS::post("RENBAN2"),          // 追加連番
                             "OUTPUT_DATA"      =>  VARS::post("OUTPUT_DATA"),      // 出力区分
                             "KUBUN"            =>  VARS::post("KUBUN"),            // 印刷指定
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    // 年組
                     );

        $this->cmd = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class' && $this->cmd != 'csv') $this->select_opt = array();
    }

    // ＣＳＶ出力
    function getDownloadModel()
    {
        $header = array("RENBAN"    => "通し番号",
                        "NAME_SEI"  => "姓",
                        "NAME_MEI"  => "名",
                        "KANA_SEI"  => "姓(かな)",
                        "KANA_MEI"  => "名(かな)",
                        "BIRTHDAY"  => "生年月日",
                        "ADDR"      => "住所",
                        "GRADE"     => "年",
                        "HR_CLASS"  => "組",
                        "ATTENDNO"  => "番号",
                        "SCHREGNO"  => "学籍番号",
                        "JUGYOU1"   => "1期授業料",
                        "JUGYOU2"   => "2期授業料",
                        "JUGYOU3"   => "3期授業料");

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
                $studentQuery = knjp181kQuery::getSchno($this, $val);
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
            $maxRenban = $this->field["RENBAN1"];
        } else if ($this->field["UPDATE_DIV"] == 2) {
            $maxRenban = $this->field["RENBAN2"];
        }

        $setRendban = $maxRenban;
        foreach ($arr_schregNo as $key => $val) {
            if ($this->field["UPDATE_DIV"] != 3) {
                if ($this->field["UPDATE_DIV"] == 2) {
                    $query = knjp181kQuery::deleteSql($this, $val);
                    $db->query($query);
                }
                //データ作成
                $query = knjp181kQuery::reductionAuthorizeDatIns($val, $setRendban);
                $db->query($query);
            }

            $query = knjp181kQuery::selectCsvData($this, $val);
            $csvRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($this->field["UPDATE_DIV"] != 3) {
                $csvRow["RENBAN"] = sprintf('%04d', $setRendban);
            }
            list($sei, $mei) = preg_split("　", $csvRow["NAME"]);
            $csvRow["NAME"] = $sei;
            $csvRow["NAME_MEI"] = $mei;
            list($sei, $mei) = preg_split("　", $csvRow["NAME_KANA"]);
            $csvRow["NAME_KANA"] = $sei;
            $csvRow["KANA_MEI"] = $mei;
            $csvRow["BIRTHDAY"] = str_replace("-", "/", $csvRow["BIRTHDAY"]);
            $csvRow = str_replace(",", ".", $csvRow);
            $contents .= implode($csvRow, ",") ."\n";
            $header_data = true;
            $setRendban++;
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
