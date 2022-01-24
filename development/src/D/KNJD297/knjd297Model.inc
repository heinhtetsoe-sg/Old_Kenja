<?php

require_once('for_php7.php');

//定数定義

class knjd297Model extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;

    //メンバ変数の初期化
    function knjd297Model()
    {
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array(
                                "SCHOOL_KIND"     => VARS::post("SCHOOL_KIND"),
                                "GRADE_HR_CLASS"  => VARS::post("GRADE_HR_CLASS")
        );

        $temp = VARS::post("year");
        if (isset($temp)) {
            $this->year          = VARS::post("year");
        }else{
            $this->year          = CTRL_YEAR;           //現在処理年度を初期値にする
        }
        $this->selectdata        = VARS::post("selectdata");
        $this->cmd               = VARS::request("cmd");

        /**************/
        /*  ヘッダー  */
        /**************/
        $this->header = array();
        $this->header["SCHREGNO"]    = "学籍番号";
        $this->header["GRADE"]       = "学年";
        $this->header["HR_CLASS"]    = "組";
        $this->header["ATTENDNO"]    = "出席番号";
        $this->header["NAME"]        = "氏名";
        $this->header["HYOUTEI5"]    = "評定5";
        $this->header["HYOUTEI4"]    = "評定4";
        $this->header["HYOUTEI3"]    = "評定3";
        $this->header["HYOUTEI2"]    = "評定2";
        $this->header["HYOUTEI1"]    = "評定1";
        $this->header["SCORE_SUM"]   = "合計点";
        $this->header["SCORE_AVG"]   = "平均点";
        $this->header["ABSCENCE"]    = "欠席";
        $this->header["LATE"]        = "遅刻";
        $this->header["EARLY"]       = "早退";
        $this->header["RECRANK"]     = "学年推薦順位";
        $this->header["PRETESTRANK"] = "模試順位";
        $this->header["PRETESTNUM"]  = "模試受験者数";
        $this->header["KEIZOKU"]     = "継続";
        $this->header["TEISHI"]      = "停止";
        $this->header["ADOPT"]       = "採用";
        $this->header["REMARK"]      = "備考";
        $this->header["STAFFREMARK"] = "担任所見";

    }
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $contents .= implode($this->header, ",")."\n";
        $existsDataFlg = false;


        /********************/
        /*  データ部分出力  */
        /********************/
        $query = "";
        $query = knjd297Query::selectMainCsvQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //カンマ変換
                $row = str_replace("\r\n","",str_replace(",",".",$row));
                $array = $this->header;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $existsDataFlg = true;
            }
            $result->free();
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $title = CTRL_YEAR.'年度_特待生選考資料';

        Query::dbCheckIn($db);
        
       
        //ＣＳＶ出力
        if ($existsDataFlg) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
        
    }


    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択して下さい。";
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjd297Query::getUpdateQuery($this);
        return $result;
    }
}
?>
