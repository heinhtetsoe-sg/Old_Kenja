<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjj130Model extends Model {
    var $cmd;
    var $field;
    var $selectdata = array();
    var $selectdata2 = array();

    /**
     * メンバ変数の初期化
     */
    function knjj130Model()
    {
    }

    function init()
    {
        $this->field = array("SCHKIND"       =>  VARS::post("SCHKIND")       //校種
                            );
        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->selectdata2   = VARS::post("selectdata2");
        $setyear = CTRL_YEAR;
        $this->wareki_year = common::Calc_Wareki($setyear, "","");
        $this->wareki_year = $this->wareki_year.'度';
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    
    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        $header = array("レベル",
                        "大会名称",
                        "日付",
                        "性別",
                        "種目",
                        "区分",
                        "成績",
                        "年組",
                        "番号",
                        "生徒名",
                        "中学校名",
                        "備考"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        //CSVデータを取得し出力変数にセット
        $data = false;
        $count = "";
        $dataContents = "";
        $query = knjj130Query::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //最初はヘッダーを付与
            if ($count == 0) {
                $setclubcd = $row["CLUBCD"];
                $clubname = "";
                $clubname = $row["CLUBCD"].':'.$row["CLUBNAME"].$lf;
                $dataContents .= $clubname;
                $dataContents .= $headerContents;
                $dataContents .= $row["HOSTNAME"]. ",";
                $dataContents .= $row["MEET_NAME"]. ",";
                $dataContents .= $row["DETAIL_DATE"]. ",";
                $dataContents .= $row["SEX_NAME"]. ",";
                $dataContents .= $row["KINDNAME"]. ",";
                $dataContents .= $row["DIV_NAME"]. ",";
                $dataContents .= $row["RECORDNAME"]. ",";
                $dataContents .= $row["HR_NAME"]. ",";
                $dataContents .= $row["ATTENDNO"]. ",";
                $dataContents .= $row["NAME"]. ",";
                $dataContents .= $row["FINSCHOOL_NAME"]. ",";
                $dataContents .= str_replace(array("\r\n","\r","\n", ","), '', $row["REMARK"]);
                $dataContents .= $lf;
            //クラブが異なるごとにヘッダーを付与
            } else if ($setclubcd != $row["CLUBCD"]) {
                $setclubcd = $row["CLUBCD"];
                $clubname = "";
                $clubname = $row["CLUBCD"].':'.$row["CLUBNAME"].$lf;
                $dataContents .= $clubname;
                $dataContents .= $headerContents;
                $dataContents .= $row["HOSTNAME"]. ",";
                $dataContents .= $row["MEET_NAME"]. ",";
                $dataContents .= $row["DETAIL_DATE"]. ",";
                $dataContents .= $row["SEX_NAME"]. ",";
                $dataContents .= $row["KINDNAME"]. ",";
                $dataContents .= $row["DIV_NAME"]. ",";
                $dataContents .= $row["RECORDNAME"]. ",";
                $dataContents .= $row["HR_NAME"]. ",";
                $dataContents .= $row["ATTENDNO"]. ",";
                $dataContents .= $row["NAME"]. ",";
                $dataContents .= $row["FINSCHOOL_NAME"]. ",";
                $dataContents .= str_replace(array("\r\n","\r","\n", ","), '', $row["REMARK"]);
                $dataContents .= $lf;
            } else {
                $dataContents .= $row["HOSTNAME"]. ",";
                $dataContents .= $row["MEET_NAME"]. ",";
                $dataContents .= $row["DETAIL_DATE"]. ",";
                $dataContents .= $row["SEX_NAME"]. ",";
                $dataContents .= $row["KINDNAME"]. ",";
                $dataContents .= $row["DIV_NAME"]. ",";
                $dataContents .= $row["RECORDNAME"]. ",";
                $dataContents .= $row["HR_NAME"]. ",";
                $dataContents .= $row["ATTENDNO"]. ",";
                $dataContents .= $row["NAME"]. ",";
                $dataContents .= $row["FINSCHOOL_NAME"]. ",";
                $dataContents .= str_replace(array("\r\n","\r","\n", ","), '', $row["REMARK"]);
                $dataContents .= $lf;
            }
            
            $count++;
            $data = true;
        }
        //}

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        //タイトル
        $title = "";
        $title = $this->wareki_year.'　大会結果一覧'.$lf;

        $contents  = "";
        $contents .= $title;
        //$contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $this->wareki_year.'大会結果一覧');
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }    
}
?>
