<?php

require_once('for_php7.php');

class knjl457hModel extends Model
{
    public $field  = array();
    public $cmd;
    public $examYear;
    public $syogakudivList;

    public function init()
    {
        $this->examYear   = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //回数
                             "SYOGAKUDIV"   =>  VARS::post("SYOGAKUDIV"),       //奨学区分
                             "NOTICE_DATE"  =>  VARS::post("NOTICE_DATE"),      //決定発行通知日
                            );

        $this->cmd = VARS::request("cmd");

        //奨学区分リスト
        $this->syogakudivList = array(
            "1" => "成績優秀",
            "2" => "クラブ",
        );
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        //ヘッダー
        //高校 成績優秀
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["SYOGAKUDIV"] == "1") {
            $csvItem = array("RENBAN"             => "連番",
                             "HAKKANNO"           => "発刊番号", //空白
                             "RECEPTNO"           => "受験番号",
                             "NAME"               => "漢字氏名",
                             "NAME_KANA"          => "かなしめい",
                             "SYOGAKUDIV"         => "推薦区分",
                             "SYOGAKU_HANTEI"     => "奨学金", //全奨、半奨
                             "SYOGAKUKIN"         => "金額", //空白
                             "HAKKOBI"            => "発行日",
                             "RENZOKU_RECEPTNO"   => "連続受験",
                            );
        //高校 クラブ
        } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["SYOGAKUDIV"] == "2") {
            $csvItem = array("RENBAN"             => "連番",
                             "HAKKANNO"           => "発刊番号", //空白
                             "RECEPTNO"           => "受験番号",
                             "NAME"               => "漢字氏名",
                             "SEX"                => "性別",
                             "FINSCHOOL_NAME"     => "中学校名",
                             "CLUB_NAME"          => "クラブ名", //空白
                             "SYOGAKUDIV"         => "クラブ推薦区分",
                             "SYOGAKUKIN"         => "金額", //空白
                             "BLANK"              => "空欄", //空白
                             "HAKKOBI"            => "発行日",
                             "RENZOKU_RECEPTNO"   => "連続受験",
                            );
        //中学 成績優秀
        } elseif ($this->field["APPLICANTDIV"] == "1" && $this->field["SYOGAKUDIV"] == "1") {
            $csvItem = array("RENBAN"             => "連番",
                             "HAKKANNO"           => "発刊番号割印", //空白
                             "RECEPTNO"           => "受験番号",
                             "NAME"               => "漢字氏名",
                             "SYOGAKUDIV"         => "奨学金種類",
                             "SYOGAKU_HANTEI"     => "全半", //全奨、半奨
                             "SYOGAKUKIN"         => "金額", //空白
                             "NOTICE_DATE"        => "決定通知発行日",
                             "REMARKS"            => "備考",
                            );
        }

        //クリア
        $contents  = "";
        //出力フラグ
        $data = false;

        //ヘッダセット
        $contents .= implode(",", $csvItem)."\n";

        $db = Query::dbCheckOut();
        $query = knjl457hQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));

            //値をセット
            $sep = "";
            foreach ($csvItem as $field => $label) {
                //推薦区分
                if ($field == "SYOGAKUDIV") {
                    if ($this->field["SYOGAKUDIV"] == "1") {
                        $row[$field] = "成績優秀";
                    } else {
                        $row[$field] = "強化クラブ推薦奨学生";
                    }
                    //発行日
                } elseif ($field == "HAKKOBI") {
                    $row[$field] = str_replace("-", "/", CTRL_DATE);
                //決定通知発行日
                } elseif ($field == "NOTICE_DATE") {
                    $row[$field] = common::DateConv1(str_replace("-", "/", $this->field["NOTICE_DATE"]), 0);
                }

                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data = true;
        }
        $result->free();

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        $query = knjl457hQuery::getNameMst($this, "L003", $this->field["APPLICANTDIV"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $applicantDivName = $row["NAME1"];

        $testdivName = "";
        if ($this->field["TESTDIV"] != "ALL") {
            $query = knjl457hQuery::getSettingMst($this, "L004", $this->field["TESTDIV"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $testdivName = " ".$row['NAME1'];
        }

        //奨学区分名
        $syogakudivListName = " ".$this->syogakudivList[$this->field["SYOGAKUDIV"]];

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s年度入試奨学生データ抽出（%s%s%s）.csv", $this->examYear, $applicantDivName, $testdivName, $syogakudivListName);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
