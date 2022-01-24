<?php
class knjl111iModel extends Model
{
    public $field  = array();
    public $cmd;
    public $examYear;

    public function init()
    {
        $this->examYear   = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                            );

        $this->cmd = VARS::request("cmd");
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {

        //ヘッダー
        $header = array("ENTEXAMYEAR"      => "入試年度",
                        "TESTDIV"          => "入試区分コード",
                        "EXAMNO"           => "受験番号",
                        "NAME"             => "志願者氏名",
                        "NAME_KANA"        => "志願者氏名カナ",
                        "SEX"              => "性別コード",
                        "ATTEND_ALL_FLG"   => "出欠区分コード",
                        "JUDGEMENT"        => "合格区分コード",
                        "PROCEDUREDIV1"    => "1次入学手続き済みコード",
                        "PROCEDUREDIV2"    => "2次入学手続き済みコード",
                        "PROCEDUREDIV3"    => "招集日参加フラグ",
                        "ENTDIV"           => "入学辞退フラグ",
                        "BIRTHDAY_1"       => "生年月日",
                        "FS_AREA_DIV"      => "出身校地区コード",
                        "FS_AREA_NAME"     => "出身校地区名称",
                        "FS_CD"            => "出身校コード",
                        "FINSCHOOL_NAME"   => "出身校名",
                        "FS_YM"            => "卒業年月",
                        "ZIPCD"            => "志願者郵便番号",
                        "ADDRESS1"         => "志願者住所１",
                        "ADDRESS2"         => "志願者住所２",
                        "TELNO"            => "志願者電話番号",
                        "GNAME"            => "保護者氏名",
                        "GKANA"            => "保護者氏名カナ",
                        "GZIPCD"           => "保護者郵便番号",
                        "GADDRESS1"        => "保護者住所１",
                        "GADDRESS2"        => "保護者住所２",
                        "GTELNO"           => "保護者電話番号",
                        "GTELNO2"          => "保護者携帯番号",
                        "EXAM_SCORE1"      => "科目名",
                        "EXAM_SCORE2"      => "科目名",
                        "EXAM_SCORE3"      => "科目名",
                        "INTERVIEW_A"      => "面接評価結果",
                        "OTHER_REMARK_1" => "資料評価１",
                        "OTHER_REMARK_2" => "資料評価２",
                        "OTHER_REMARK_3" => "資料評価３",
                        "OTHER_REMARK_4" => "資料評価４",
                        "OTHER_REMARK_5" => "資料評価５",
                        "OTHER_REMARK_6" => "資料評価６",
                        "OTHER_REMARK_7" => "資料評価７",
                        "TOTAL1"           => "受験科目合計点",
                        "AVARAGE1"         => "受験科目平均点",
                        "JUDGE_DEVIATION"  => "受験科目偏差値",
                        "TOTAL_RANK1"      => "全体序列",
                        "SEX_RANK1"        => "性別序列"
                        );

        //クリア
        $contents  = "";
        //出力フラグ
        $data = false;

        $db = Query::dbCheckOut();

        //科目取得
        $testsubclassList = array();
        $result = $db->query(knjl111iQuery::getTestSubclassList($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubclassList[$row["SEQ"]] = $row["NAME1"];
            $header["EXAM_SCORE".$row["SEQ"]] = $row["NAME1"];
        }

        //ヘッダ
        $contents .= implode(",", $header)."\n";

        $query = knjl111iQuery::selectCsvQuery($this, $testsubclassList);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));

            //値をセット
            $sep = "";
            foreach ($row as $field => $label) {
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
        $applicantDivName = $db->getOne(knjl111iQuery::getApplicantDivName($this));
        $testdivAbbv = $db->getOne(knjl111iQuery::getEntexamTestDivAbbv($this));

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            $filename = sprintf("%s年度入試受験結果CSV出力（%s%s）.csv", $this->examYear, $applicantDivName, $testdivAbbv);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
