<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje152aModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knje152aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付 /* NO003 */
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
//                           "MEN"              =>  VARS::post("MEN"),              //印刷面
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        $header = array();
        $header["SCHREGNO"]         = "学籍番号";
        $header["NAME"]             = "氏名";
        //$header["CLASSCD"]          = "教科コード";
        //$header["SCHOOL_KIND"]      = "校種";
        $header["C_CD"]             = "科目コード";
        $header["SUBCLASSNAME1"]    = "開設科目名";
        $header["SUBCLASSNAME2"]    = "教育課程用科目名";
        $header["COURSEDIVNAME"]    = "必履修区分";
        $header["YEAR"]             = "単位修得年度";
        $header["VALUATION"]        = "評定";
        $header["CREDIT"]           = "修得単位数";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        $this->field["HEADER"] = "on";

        // データ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = "";
        $query = knje152aQuery::selectMainQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                //カンマ変換
                $row = str_replace("\r\n","",str_replace(",",".",$row));
                $array = $header;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $title = '成績レポート';

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }//getDownloadModelの綴り

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
