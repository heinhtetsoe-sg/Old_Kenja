<?php

require_once('for_php7.php');
class knjl295yModel extends Model
{
    public $field      = array();
    public $ObjYear;
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjl295yModel()
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

        //ヘッダ
        //志願者情報
        if ($this->field["OUTPUT"] == "1") {
            $header = array("EXAMNO"            => "受験番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "かな氏名",
                            "SEX_NAME"          => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "FS_NAME"           => "出身学校名",
                            "TELNO"             => "電話番号",
                            "ZIPCD"             => "郵便番号",
                            "ADDRESS"           => "住所",
                            "GNAME"             => "保護者名",
                            "GKANA"             => "保護者かな氏名",
                            "RELATIONSHIP_NAME" => "続柄",
                            "REMARK1"           => "志願者理由",
                            "REMARK2"           => "ご家族で大切にしているもの",
                            "REMARK3"           => "本人の長所",
                            "REMARK4"           => "本人の短所",
                            "BIKOU"             => "家族・備考",
                            $this->lastColumn   => $this->lastColumn);
        //試験情報
        } else {
            //傾斜得点
            if ($this->field["HAITEN"] == "1") {
                $setTotal = "TOTAL2";
                $setTotalRank = "TOTAL_RANK2";
                $setTotal2 = "TOTAL4";
                $setTotalRank2 = "TOTAL_RANK4";
            } else {
                $setTotal = "TOTAL1";
                $setTotalRank = "TOTAL_RANK1";
                $setTotal2 = "TOTAL3";
                $setTotalRank2 = "TOTAL_RANK3";
            }

            $header = array("EXAMNO"            => "受験番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "かな氏名",
                            "SEX_NAME"          => "性別",
                            "FS_NAME"           => "出身学校名",
                            "SCORE1"            => "得点(言葉)",
                            "SCORE2"            => "得点(パズル)",
                            "SCORE3"            => "得点(生活)",
                            "SCORE4"            => "得点(かず)",
                            $setTotal           => "筆記試験合計",
                            $setTotalRank       => "順位",
                            "INTERVIEW_VALUE2_NAME"  => "行動の評定",
                            $setTotal2          => "合計",
                            $setTotalRank2      => "順位",
                            "INTERVIEW_REMARK2" => "行動観察備考",
                            "INTERVIEW_REMARK"  => "面接備考(本人用)",
                            "INTERVIEW_REMARK3" => "面接備考(保護者用)",
                            "JUDGEMENT"         => "判定",
                            "PROCEDUREDATE"     => "入金日",
                            $this->lastColumn   => $this->lastColumn);
        }

        $contents = "";
        $contents = implode($header, ",") ."\n";

        //CSVデータ取得
        $dataFlg  = false;
        $query  = knjl295yQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            
            //志願者情報
            if ($this->field["OUTPUT"] == "1") {
                $row["BIRTHDAY"] = str_replace("-", ".", $row["BIRTHDAY"]);
                /***/
                $row["REMARK1"] = str_replace("\r", "", $row["REMARK1"]);
                $row["REMARK1"] = str_replace("\n", "", $row["REMARK1"]);
                $row["REMARK2"] = str_replace("\r", "", $row["REMARK2"]);
                $row["REMARK2"] = str_replace("\n", "", $row["REMARK2"]);
                $row["REMARK3"] = str_replace("\r", "", $row["REMARK3"]);
                $row["REMARK3"] = str_replace("\n", "", $row["REMARK3"]);
                $row["REMARK4"] = str_replace("\r", "", $row["REMARK4"]);
                $row["REMARK4"] = str_replace("\n", "", $row["REMARK4"]);
                /***/
                //家族・備考
                $count = 0;
                $row["BIKOU"] = "";
                $query  = knjl295yQuery::getFamilyCsvDataQuery($this, $row["EXAMNO"]);
                $resultFamily = $db->query($query);
                while ($rowFamily = $resultFamily->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($count == 0) {
                        $shasen = "";
                    } else {
                        $shasen = "／";
                    }
                    $rowFamily = str_replace(",", ".", $rowFamily);
                    $rowFamily = str_replace("\"", "", $rowFamily);
                    $rowFamily["FAMILY_REMARK"] = str_replace("\r", "", $rowFamily["FAMILY_REMARK"]);
                    $rowFamily["FAMILY_REMARK"] = str_replace("\n", "", $rowFamily["FAMILY_REMARK"]);
                    $row["BIKOU"] .= $shasen.$rowFamily["FAMILY_NAME"].' '.$rowFamily["AGE"].'歳 '.$rowFamily["FAMILY_RELATIONSHIP_NAME"].' '.$rowFamily["FAMILY_REMARK"];
                    $count++;
                }
                $resultFamily->free();
            
            //試験情報
            } else {
                $row["INTERVIEW_REMARK2"] = str_replace("\r", "", $row["INTERVIEW_REMARK2"]);
                $row["INTERVIEW_REMARK2"] = str_replace("\n", "", $row["INTERVIEW_REMARK2"]);
                $row["INTERVIEW_REMARK"] = str_replace("\r", "", $row["INTERVIEW_REMARK"]);
                $row["INTERVIEW_REMARK"] = str_replace("\n", "", $row["INTERVIEW_REMARK"]);
                $row["INTERVIEW_REMARK3"] = str_replace("\r", "", $row["INTERVIEW_REMARK3"]);
                $row["INTERVIEW_REMARK3"] = str_replace("\n", "", $row["INTERVIEW_REMARK3"]);

                $row["PROCEDUREDATE"] = str_replace("-", ".", $row["PROCEDUREDATE"]);
            }
            
            $row[$this->lastColumn] = $this->lastColumn;
            
            //出力項目をセット
            $outputCnt = 0;
            foreach ($header as $key => $val) {
                if ($outputCnt == 0) {
                    $conma = "";
                } else {
                    $conma = ",";
                }
                $contents .= $conma.$row[$key];
                $outputCnt++;
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
            if ($this->field["OUTPUT"] == "1") {
                $filename = $this->ObjYear."年度"." 小学校入学試験志願者情報_".$testdivName.".csv";
            } else {
                if ($this->field["HAITEN"] == "1") {
                    $filename = $this->ObjYear."年度"." 小学校入学試験試験情報(傾斜得点)_".$testdivName.".csv";
                } else {
                    $filename = $this->ObjYear."年度"." 小学校入学試験試験情報(通常得点)_".$testdivName.".csv";
                }
            }
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
