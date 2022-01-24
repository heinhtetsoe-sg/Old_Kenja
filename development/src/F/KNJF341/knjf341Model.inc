<?php

require_once('for_php7.php');

class knjf341Model extends Model
{
    public $cmd;
    public $control_data = array();//コントロールのデータを保持
    public $staffcd;               //セキュリティーチェック
    public $sec_competence;        //権限保持
    public $updated = array();     //UPDATED値保持
    public $schoolcd;              //学校コード
    public $execute_date;          //県への報告用登録日付
    public $docNumber;

    public function knjf341Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->execute_date = VARS::post("EXECUTE_DATE");
        $this->fixedDate    = VARS::post("FIXED_DATE");
        $this->fixedData    = VARS::post("FIXED_DATA");
        $this->title        = "尿検査結果集計";

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, 'KNJF341');

        //画面の入力値を保持
        $this->dataFieldKeys = array(
            'TARGETS1'    ,
            'EXAMINEE1'   ,
            'REMARK1'     ,
            'URICSUGAR1'  ,
            'ALBUMINURIA1',
            'URICBLEED1'  ,
            'TARGETS2'    ,
            'EXAMINEE2'   ,
            'TARGETS3'    ,
            'EXAMINEE3'   ,
            'NORMAL'      ,
            'CAREFUL'     ,
            'TREATMENT'   ,
        );
        $this->updField = array();
        $tempArray = array();
        foreach ($this->dataFieldKeys as $keyName) {
            $tempVal = VARS::post($keyName);

            //理由は解らないが、サーバーにより$tempValが配列になる場合とならない場合があるため
            $tempVal = is_array($tempVal) ? $tempVal[0] : $tempVal;

            $tempArray[$keyName] = $tempVal;
        }

        $this->updField[$i] = $tempArray;

        //V_SCHOOL_MSTから学校コードを取得
        $this->schoolcd = knjf341Query::getSchoolMst();

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新ボタン押下時の処理
    public function &getUpdateModel()
    {
        knjf341Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //確定処理＞確定ボタン押下時の処理
    public function &getFixedUpdateModel()
    {
        knjf341Query::getFixedUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //県への報告ボタン押下時の処理
    public function &getUpdateEdboardModel()
    {
        knjf341Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //CSV出力ボタン押下時
    public function getDownloadModel()
    {
        $CSV_EL = "\n";

        //CSVデータとなる文字列変数
        $contents  = "";

        //ヘッダー行追加
        $header = array(
            '学校名'          ,
            '一次対象者'      ,
            '一次受検者'      ,
            '一次受検率％'    ,
            '一次有所見'      ,
            '一次糖'          ,
            '一次蛋白'        ,
            '一次潜血'        ,
            '再検査対象者'    ,
            '再検査実施数'    ,
            '再検査実施率％'  ,
            '精密検査該当者'  ,
            '受検者数'        ,
            '異常なし'        ,
            '要観察'          ,
            '要治療'          ,
            '精密検査受診率％',
        );
        $contents .= implode(',', $header) . $CSV_EL;

        //学校名
        $schoolName = knjf341Query::getEdboardSchoolName();

        //データ行追加
        $dataExist = false;
        $db = Query::dbCheckOut();
        $query = knjf341Query::getCsvData($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名をセット
            if ($this->fixedData) {
            } else {
                $row['SCHOOLNAME'] = $schoolName;
            }

            //パーセント表記にする
            $row['PERCENT1'] = ($row['PERCENT1'] != '')? ($row['PERCENT1'] . '%'): '0.0%';
            $row['PERCENT2'] = ($row['PERCENT2'] != '')? ($row['PERCENT2'] . '%'): '0.0%';
            $row['PERCENT3'] = ($row['PERCENT3'] != '')? ($row['PERCENT3'] . '%'): '0.0%';

            //出力しないデータを削除
            unset($row['EDBOARD_SCHOOLCD']);

            $row = str_replace(',', '.', $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ',') . $CSV_EL;

            $dataExist = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataExist) {
            $filename = sprintf('%s.csv', $this->title.'報告');

            common::downloadContents($contents, $filename, "SJIS-win");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
