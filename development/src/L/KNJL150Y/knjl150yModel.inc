<?php

require_once('for_php7.php');
class knjl150yModel extends Model
{
    public $field      = array();
    public $ObjYear;
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjl150yModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度コンボ
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分コンボ
                             "OUTPUT"       =>  VARS::post("OUTPUT"),           //出力情報ラジオ
                             "HAITEN"       =>  VARS::post("HAITEN")            //配点ラジオ
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

        $outputName = ($this->field["OUTPUT"] == "1") ? "志願者情報" : "試験情報";
        //志願者情報
        if ($this->field["OUTPUT"] == "1") {
            if ($this->field["APPLICANTDIV"] == "1") {
                //ヘッダ(中学)
                $header = array("ENTEXAM_YEAR"          => "入試年度",
                                "ENTEXAM_APPLICANTDIV"  => "入試制度",
                                "ENTEXAM_TESTDIV"       => "入試区分",
                                "RECOM_ITEM_NAME"   => "推薦事項",
                                "EXAMNO"            => "受験番号",
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "かな氏名",
                                "SEX_NAME"          => "性別",
                                "BIRTHDAY"          => "生年月日",
                                "FINSCHOOL_NAME"    => "出身学校名",
                                "FS_GRDYEAR_M"      => "卒年月",
                                "FS_GRDDIV_NAME"    => "卒見込",
                                "TELNO"             => "電話番号",
                                "ZIPCD"             => "郵便番号",
                                "ADDRESS"           => "住所",
                                "GNAME"             => "保護者名",
                                "GKANA"             => "保護者かな氏名",
                                "RELATIONSHIP_NAME" => "続柄",
                                "GTELNO"            => "保護者・電話番号",
                                "GZIPCD"            => "保護者・郵便番号",
                                "GADDRESS"          => "保護者・住所",
                                "GENERAL"           => "一般希",
                                "DORMITORY"         => "入寮希",
                                "REMARK1"           => "備考１（志願動機）",
                                "REMARK2"           => "備考２",
                                "AVERAGE_ALL"       => "評定合計",
                                "ABSENCE_DAYS1"     => "欠席日数（１学期）",
                                "ABSENCE_DAYS2"     => "欠席日数（２学期）",
                                "ABSENCE_DAYS3"     => "欠席日数（３学期）",
                                "ACTIVITY"          => "活動",
                                "SECTION"           => "所属",
                                "RESULTS"           => "主な実績",
                                $this->lastColumn   => $this->lastColumn);
            } else {
                //ヘッダ(高校)
                $header = array("INTERVIEW_ATTEND"  => "帰国生",
                                "ENTEXAM_YEAR"          => "入試年度",
                                "ENTEXAM_APPLICANTDIV"  => "入試制度",
                                "ENTEXAM_TESTDIV"       => "入試区分",
                                "DESIREDIV_NAME"    => "志望区分",
                                "RECOM_KIND_NAME"   => "推薦区分",
                                "EXAMNO"            => "受験番号",
                                "SHDIV_NAME"        => "専併区分",
                                "SH_SCHOOL_NAME"    => "併願校",
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "かな氏名",
                                "SEX_NAME"          => "性別",
                                "BIRTHDAY"          => "生年月日",
                                "FINSCHOOL_NAME"    => "出身学校名",
                                "FS_GRDYEAR_M"      => "卒年月",
                                "FS_GRDDIV_NAME"    => "卒見込",
                                "EXAMCOURSE_NAME1"  => "志望コース１",
                                "EXAMCOURSE_NAME2"  => "志望コース２",
                                "TELNO"             => "電話番号",
                                "ZIPCD"             => "郵便番号",
                                "ADDRESS"           => "住所",
                                "GNAME"             => "保護者名",
                                "GKANA"             => "保護者かな氏名",
                                "RELATIONSHIP_NAME" => "続柄",
                                "GTELNO"            => "保護者・電話番号",
                                "GZIPCD"            => "保護者・郵便番号",
                                "GADDRESS"          => "保護者・住所",
                                "SHIFT_DESIRE"      => "特判希",
                                "TTOKU"             => "Ｔ特希",
                                "SLIDE"             => "スラ希",
                                "GENERAL"           => "一般希",
                                "GENERAL_DESIREDIV_NAME"    => "一般希・志望区分",
                                "GENERAL_SHDIV_NAME"        => "一般希・専併区分",
                                "SPORTS"            => "ス優希",
                                "DORMITORY"         => "入寮希",
                                "REMARK1"           => "備考１",
                                "REMARK2"           => "備考２",
                                "AVERAGE_ALL"       => "評定合計",
                                "ABSENCE_DAYS1"     => "欠席日数（１年）",
                                "ABSENCE_DAYS2"     => "欠席日数（２年）",
                                "ABSENCE_DAYS3"     => "欠席日数（３年）",
                                "ACTIVITY"          => "活動",
                                "SECTION"           => "所属",
                                "RESULTS"           => "主な実績",
                                $this->lastColumn   => $this->lastColumn);
            }

            //試験情報
        } else {
            if ($this->field["APPLICANTDIV"] == "1") {
                //ヘッダ(中学)
                $header = array("ENTEXAM_YEAR"          => "入試年度",
                                "ENTEXAM_APPLICANTDIV"  => "入試制度",
                                "ENTEXAM_TESTDIV"       => "入試区分",
                                "RECOM_ITEM_NAME"   => "推薦事項",
                                "EXAMNO"            => "受験番号",
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "かな氏名",
                                "SEX_NAME"          => "性別",
                                "FINSCHOOL_NAME"    => "出身学校名",
                                "EXAMHALL_NAME"     => "会場名",
                                "DETAIL_NO"         => "分割No",
                                "KOKUGO"            => "得点（国語）",
                                "SUUGAKU"           => "得点（算数）",
                                "KEISAN"            => "得点（計算）",
                                "TEKISEI"           => "得点（適性）",
                                "TOTAL"             => "合計",
                                "INTERVIEW_REMARK"  => "面接備考",
                                "INTERVIEW_VALUE"   => "面接評価",
                                "COMPOSITION_VALUE" => "作文評価",
                                "JUDGEDIV_NAME"     => "判定",
                                "PROCEDUREDATE"     => "入金日",
                                $this->lastColumn   => $this->lastColumn);
            } else {
                //ヘッダ(高校)
                $header = array("INTERVIEW_ATTEND"  => "帰国生",
                                "ENTEXAM_YEAR"          => "入試年度",
                                "ENTEXAM_APPLICANTDIV"  => "入試制度",
                                "ENTEXAM_TESTDIV"       => "入試区分",
                                "DESIREDIV_NAME"    => "志望区分",
                                "RECOM_KIND_NAME"   => "推薦区分",
                                "EXAMNO"            => "受験番号",
                                "SHDIV_NAME"        => "専併区分",
                                "SH_SCHOOL_NAME"    => "併願校",
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "かな氏名",
                                "SEX_NAME"          => "性別",
                                "FINSCHOOL_NAME"    => "出身学校名",
                                "EXAMCOURSE_NAME1"  => "志望コース１",
                                "EXAMCOURSE_NAME2"  => "志望コース２",
                                "EXAMHALL_NAME"     => "会場名",
                                "DETAIL_NO"         => "分割No",
                                "KOKUGO"            => "得点（国語）",
                                "SUUGAKU"           => "得点（数学）",
                                "SHAKAI"            => "得点（社会）",
                                "RIKA"              => "得点（理科）",
                                "EIGO"              => "得点（英語）",
                                "LISNING"           => "得点（リス）",
                                "KATEN"             => "加点",
                                "TOTAL"             => "合計",
                                "AVERAGE_ALL"       => "評定合計",
                                "INTERVIEW_REMARK"  => "面接備考",
                                "INTERVIEW_VALUE"   => "面接評価",
                                "COMPOSITION_VALUE" => "作文評価",
                                "SUC_COURSE_NAME"   => "合格コース",
                                "JUDGEDIV_NAME"     => "判定",
                                "JUDGE_KIND_NAME"   => "特奨",
                                "PROCEDUREDATE"     => "入金日",
                                $this->lastColumn   => $this->lastColumn);
            }
        }

        $contents = "";
        $contents = implode($header, ",") ."\n";

        //名称マスタ取得
        $namecd1 = ($this->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $testdivName = $db->getOne(knjl150yQuery::getNameMstCsv($namecd1, $this->field["TESTDIV"]));
        $applicantdivName = $db->getOne(knjl150yQuery::getNameMstCsv("L003", $this->field["APPLICANTDIV"]));

        //CSVデータ取得
        $dataFlg  = false;
        $query  = knjl150yQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            $row = str_replace("\r", "", $row);//データ内に改行があればブランクに置換
            $row = str_replace("\n", "", $row);//データ内に改行があればブランクに置換
            /***/
            $row["ENTEXAM_YEAR"] = $this->ObjYear;
            $row["ENTEXAM_APPLICANTDIV"] = $applicantdivName;
            $row["ENTEXAM_TESTDIV"] = $testdivName;
            $row["RECOM_ITEM_NAME"] = $row["RECOM_ITEM1"] ."." .$row["RECOM_ITEM2"] ."." .$row["RECOM_ITEM3"] ."." .$row["RECOM_ITEM4"];
            $row["BIRTHDAY"] = str_replace("-", ".", $row["BIRTHDAY"]);
            $row["PROCEDUREDATE"] = str_replace("-", ".", $row["PROCEDUREDATE"]);
            $row[$this->lastColumn] = $this->lastColumn;
            /***/
            foreach ($header as $key => $val) {
                $contents .= $row[$key] .",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            //ファイル名
            $outputName = ($this->field["OUTPUT"] == "1") ? "志願者情報" : "試験情報";
            $filename = $this->ObjYear."年度".$applicantdivName.$testdivName."_".$outputName.".csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
