<?php

require_once('for_php7.php');

class knjl410yModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl410yModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "APP_HOLD"     =>  VARS::post("APP_HOLD"),         //入試区分
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");

    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

         //ヘッダ(高校推薦入試)
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
                         "DUMMY"             => "DUMMY");

        $contents = "";
        $contents = implode($header, ",") ."\n";

        //CSVデータ取得
        $dataFlg  = false;
        $query  = knjl410yQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
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
            $query  = knjl410yQuery::getFamilyCsvDataQuery($this, $row["EXAMNO"]);
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
            
            $row["DUMMY"] = "DUMMY";
            
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

            if (!$dataFlg) $testdivName = $row["TESTDIV_NAME"];
            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            //ファイル名
            $filename = $this->ObjYear."年度"." 小学校入学試験志願者データチェックリスト_".$testdivName.".csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
