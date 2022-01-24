<?php

require_once('for_php7.php');
class knjl310yModel extends Model
{
    public $field      = array();
    public $ObjYear;
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjl310yModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV"),        //志望区分
                             "OUTPUT_C"     =>  VARS::post("OUTPUT_C"),         //特進再チャレンジ者のみ出力
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        if ($this->field["APPLICANTDIV"] == "1") {
            //ヘッダ(中学)
            $header = array("EXAMNO"            => "受験番号",
                            "RECOM_ITEM_NAME"   => "推薦区分",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "かな氏名",
                            "SEX_NAME"          => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "FINSCHOOL_NAME"    => "出身学校名",
                            "FS_GRDDIV_NAME"    => "卒見込",
                            "GNAME"             => "保護者名",
                            "GKANA"             => "保護者かな氏名",
                            "RELATIONSHIP_NAME" => "続柄",
                            "TELNO"             => "電話番号",
                            "ZIPCD"             => "郵便番号",
                            "ADDRESS"           => "住所",
                            "DORMITORY"         => "入寮希",
                            "REMARK1"           => "備考１（志願動機）",
                            "REMARK2"           => "備考２",
                            $this->lastColumn   => $this->lastColumn);
        } elseif ($this->field["TESTDIV"] == "2") {
            //ヘッダ(高校推薦入試)
            $header = array("INTERVIEW_ATTEND"  => "帰国生",
                            "DESIREDIV_NAME"    => "志望区分",
                            "RECOM_KIND_NAME"   => "推薦区分",
                            "EXAMNO"            => "受験番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "かな氏名",
                            "SEX_NAME"          => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "FINSCHOOL_NAME"    => "出身学校名",
                            "FS_GRDDIV_NAME"    => "卒見込",
                            "EXAMCOURSE_NAME1"  => "志望コース１",
                            "EXAMCOURSE_NAME2"  => "志望コース２",
                            "TELNO"             => "電話番号",
                            "ZIPCD"             => "郵便番号",
                            "ADDRESS"           => "住所",
                            "GNAME"             => "保護者名",
                            "GKANA"             => "保護者かな氏名",
                            "RELATIONSHIP_NAME" => "続柄",
                            "GENERAL"           => "T特希",
                            "SPORTS"            => "ス優希",
                            "DORMITORY"         => "入寮希",
                            "SHDIV_NAME"        => "専／併",
                            "SH_SCHOOL_NAME"    => "併願校",
                            "AVERAGE_ALL"       => "評定合計",
                            "SHIFT_DESIRE"      => "特判希",
                            "ABSENCE_DAYS1"     => "欠席日数１年",
                            "ABSENCE_DAYS2"     => "欠席日数２年",
                            "ABSENCE_DAYS3"     => "欠席日数３年",
                            "REMARK1"           => "備考１",
                            "REMARK2"           => "備考２",
                            $this->lastColumn   => $this->lastColumn);
        } else {
            //ヘッダ(高校推薦入試以外)
            $header = array("INTERVIEW_ATTEND"  => "帰国生",
                            "DESIREDIV_NAME"    => "志望区分",
                            "EXAMNO"            => "受験番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "かな氏名",
                            "SEX_NAME"          => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "FINSCHOOL_NAME"    => "出身学校名",
                            "FS_GRDDIV_NAME"    => "卒見込",
                            "EXAMCOURSE_NAME1"  => "志望コース１",
                            "EXAMCOURSE_NAME2"  => "志望コース２",
                            "TELNO"             => "電話番号",
                            "ZIPCD"             => "郵便番号",
                            "ADDRESS"           => "住所",
                            "GNAME"             => "保護者名",
                            "GKANA"             => "保護者かな氏名",
                            "RELATIONSHIP_NAME" => "続柄",
                            "GENERAL"           => "T特希",
                            "SPORTS"            => "ス優希",
                            "DORMITORY"         => "入寮希",
                            "SHDIV_NAME"        => "専／併",
                            "SH_SCHOOL_NAME"    => "併願校",
                            "AVERAGE_ALL"       => "評定合計",
                            "SHIFT_DESIRE"      => "特判希",
                            "ABSENCE_DAYS1"     => "欠席日数１年",
                            "ABSENCE_DAYS2"     => "欠席日数２年",
                            "ABSENCE_DAYS3"     => "欠席日数３年",
                            "REMARK1"           => "備考１",
                            "REMARK2"           => "備考２",
                            $this->lastColumn   => $this->lastColumn);
        }

        $contents = "";
        $contents = implode($header, ",") ."\n";

        //CSVデータ取得
        $dataFlg  = false;
        $query  = knjl310yQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            $row["RECOM_ITEM_NAME"] = $row["RECOM_ITEM1"] ."." .$row["RECOM_ITEM2"] ."." .$row["RECOM_ITEM3"] ."." .$row["RECOM_ITEM4"];
            $row["BIRTHDAY"] = str_replace("-", ".", $row["BIRTHDAY"]);
            /***/
            $row["REMARK1"] = str_replace("\r", "", $row["REMARK1"]);
            $row["REMARK1"] = str_replace("\n", "", $row["REMARK1"]);
            $row["REMARK2"] = str_replace("\r", "", $row["REMARK2"]);
            $row["REMARK2"] = str_replace("\n", "", $row["REMARK2"]);
            /***/
            $row[$this->lastColumn] = $this->lastColumn;
            foreach ($header as $key => $val) {
                $contents .= $row[$key] .",";
            }
            $contents .= "\n";

            if (!$dataFlg) {
                $testdivName = $row["TESTDIV_NAME"];
            }
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            //ファイル名
            $schoolName = ($this->field["APPLICANTDIV"] == "1") ? "中学" : "高校";
            $filename = $this->ObjYear."年度".$schoolName."入学試験志願者データチェックリスト_".$testdivName.".csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
