<?php

require_once('for_php7.php');

class knje366Model extends Model {
    var $cmd;
    var $control;

    function knje366Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        $title = "大学別進学資料";
        $contents .= $title. str_repeat(",", 10). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";

        //ヘッダ（2行目）
        $header = array("SCHOOL_GROUP"          => "系列区分",
                        "SCHOOL_GROUP_NAME"     => "学校系列",
                        "STAT_CD"               => "大学コード",
                        "SCHOOL_NAME"           => "大学",
                        "PASS_SCHOOL_CNT1"      => "合格数",
                        "PASS_SCHOOL_CNT2"      => "過年度",
                        "FACULTYCD"             => "学部コード",
                        "FACULTYNAME"           => "学部",
                        "PASS_FACULTY_CNT1"     => "合格数",
                        "PASS_FACULTY_CNT2"     => "過年度",
                        "GO_FACULTY_CNT1"       => "進学数",
                        "GO_FACULTY_CNT2"       => "過年度",
                        "DEPARTMENTCD"          => "学科コード",
                        "DEPARTMENTNAME"        => "学科",
                        "PLANSTAT"              => "進学",
                        "REGD_DIV"              => "現浪",
                        "NAME"                  => "氏名",
                        "SEX"                   => "性別",
                        "SCHREGNO"              => "学籍番号",
                        "HR_NAME"               => "年組",
                        "ATTENDNO"              => "出席番号"
                        );

        //ヘッダ（2行目）セット
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $data = false;
        $dataTemp = array();
        $query = knje366Query::getCsvQuery();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);
            $row["SCHOOL_GROUP"] = ($row["SCHOOL_GROUP"] == "9") ? "" : $row["SCHOOL_GROUP"];
            $dataTemp[] = $row;
        }
        $result->free();

        foreach ($dataTemp as $key => $val) {
            //ヘッダー順で出力
            $sep = "";
            foreach ($header as $hkey => $hval) {
                //データセット
                $contents .= $sep.$dataTemp[$key][$hkey];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $title = "大学別進学資料";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
