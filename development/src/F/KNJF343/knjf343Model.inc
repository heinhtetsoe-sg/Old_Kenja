<?php

require_once('for_php7.php');

class knjf343Model extends Model
{
    public $cmd;
    public $control_data = array();//コントロールのデータを保持
    public $staffcd;               //セキュリティーチェック
    public $sec_competence;        //権限保持
    public $updated = array();     //UPDATED値保持
    public $schoolcd;              //学校コード
    public $execute_date;          //県への報告用登録日付
    public $docNumber;

    public function knjf343Model()
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
        $this->title        = '心電図検査結果集計';

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, 'KNJF343');

        //画面の入力値を保持
        $this->dataFieldKeys = array(
            'TARGET1'             ,
            'EXAMINEE1'           ,
            'HAVE_REMARK1'        ,
            'REMARK1_CNT1'        ,
            'REMARK1_CNT2'        ,
            'REMARK1_CNT3'        ,
            'REMARK1_CNT4'        ,
            'TARGET2'             ,
            'EXAMINEE2'           ,
            'REMARK2_CNT1'        ,
            'REMARK2_CNT2'        ,
            'REMARK2_CNT3'        ,
            'REMARK2_CNT4'        ,
            'REMARK2_CNT5'        ,
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
        $this->schoolcd = knjf343Query::getSchoolMst();

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新ボタン押下時の処理
    public function &getUpdateModel()
    {
        knjf343Query::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //確定処理＞確定ボタン押下時の処理
    public function &getFixedUpdateModel()
    {
        knjf343Query::getFixedUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //県への報告ボタン押下時の処理
    public function &getUpdateEdboardModel()
    {
        knjf343Query::getUpdateReport($this);
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
            '有所見者数'      ,
            '要精検'          ,
            '主治医管理'      ,
            '放置可'          ,
            'その他'          ,
            '有所見者率％'    ,
            '精密検査該当者'  ,
            '精密検査受検者数',
            '精密検査受検率％',
            '異常なし'        ,
            '要医療'          ,
            '要観察'          ,
            '放置可'          ,
            'その他'          ,
        );
        $contents .= implode(',', $header) . $CSV_EL;

        //学校名
        $schoolName = knjf343Query::getEdboardSchoolName();

        //データ行追加
        $dataExist = false;
        $db = Query::dbCheckOut();
        $query = knjf343Query::getCsvData($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名をセット
            if ($this->fixedData) {
            } else {
                $row['SCHOOLNAME'] = $schoolName;
            }

            //パーセント表記にする
            $row['PERCENT1']            = ($row['PERCENT1']            != '')? ($row['PERCENT1'] .            '%'): '0.0%';
            $row['HAVE_REMARK_PERCENT'] = ($row['HAVE_REMARK_PERCENT'] != '')? ($row['HAVE_REMARK_PERCENT'] . '%'): '0.0%';
            $row['PERCENT2']            = ($row['PERCENT2']            != '')? ($row['PERCENT2'] .            '%'): '0.0%';

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
