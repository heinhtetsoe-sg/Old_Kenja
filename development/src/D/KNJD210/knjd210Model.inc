<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd210Model extends Model {
    var $field      = array();
    var $cmd;
    var $data;
    var $sort;              //昇順・降順
    var $gradingclasscd;    //表示科目
    var $alert;             //エラーメッセージ
    var $average;           //平均点
    var $assesslevel;           //評価最大値
    var $replace;           //講座読替え
    var $merge;             //講座合併
    
    /**
     * メンバ変数の初期化
     */
    function knjd210Model()
    {
        $this->sort["ATTENDNO"] = 1;
        $this->field = array();
        $this->field["CHAIRCD"] = array();
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        //学習記録エクスプローラより取得
        if (VARS::post("GTREDATA")){
            $this->field = unserialize(VARS::post("GTREDATA"));
        }
        //初回起動
        if (VARS::get("start")){
            $this->field["CHAIRCD"] = array();
            $this->field["APPDATE"] = array();
        }
        $this->data["VALUATION"]    = VARS::post("VALUATION");              //評価
        $this->data["GET_CREDIT"]   = VARS::post("GET_CREDIT");             //取得単位
        $this->data["ADD_CREDIT"]   = VARS::post("ADD_CREDIT");             //増加区分
        $this->data["REMARK"]       = VARS::post("REMARK");                 //備考
        //相対評価処理
        $this->data["ASSESSLOW"]    = VARS::post("ASSESSLOW");              //相対評価処理下限
        $this->data["RELATIVED_FLG"] = VARS::post("RELATIVED_FLG");           //相対評価処理フラグ

        $this->checked = VARS::post("CHECKED");          //チェック
        //表示科目
        if (VARS::post("GTREDATA")){
            unset($this->gradingclasscd);
        }else if (VARS::post("GRADINGCLASSCD")){
            $this->gradingclasscd = VARS::post("GRADINGCLASSCD");
        }
        $this->cmd     = VARS::request("cmd");
    }
    /**
     * 仮評価処理モデルを返します
    */
    function &getEstimateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        //成績期末データの作成
        knjd210Query::addNewRecord($db, $this);
        /*  仮評価処理 */
        knjd210Query::estimate($db, $this);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }
    /**
     * 全体表示用モデルを返します
    */
    function &getMainModel()
    {
        //ソート用 番号、席次
        $sort = array("ATTENDNO","RANK");
        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            foreach($sort as $field){
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                }elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                }else{
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }
    /**
     * 平均点補正モデルを返します
    */
    function &getAvgModel()
    {
        //平均点補正のデータを取得
        knjd210Query::getAverage($this);
    }
    /**
     * 相対評価実行モデルを返します
    */
    function &getExecModel()
    {
        //平均点補正のデータを取得
        knjd210Query::execAssess($this);
    }
    //入力項目チェック
    function validateInput()
    {
        if ($this->cmd == "delete" && !isset($this->checked)){
            $this->warning = "チェックボックスが選択されていません。";
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        /*  更新処理 */
        knjd210Query::update($this);
        $this->setMessage("MSG201");
    }
    /**
     * 削除用のモデルを返します
    */
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput() && isset($this->checked) ){
            //成績処理画面編集対象データの削除処理
            knjd210Query::delete($this);
        }
        $this->setMessage("MSG201");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
//var_dump($_REQUEST);
?>
