// kanji=漢字
// $Id: readme.txt 74877 2020-06-15 13:16:26Z ishii $

2011/05/13  1.新規作成

2011/05/18  1.テキスト入力可(knjdBehaviorsd_UseText)
            2.ドロップダウンリストでの選択を可能にした

2011/05/27  1.名称マスタ(D035, D036)登録無しも考慮

2011/06/07  1.取得パラメータを追加(138Jや137P毎でKNJD_BEHAVIOR_SDの設定可能なように)
            -- send_knjdBehaviorsd_UseText
            2.prgInfo.propertiesの読み込み処理修正

2011/06/14  1.send_knjdBehaviorsd_UseTextの判定を修正

2014/03/11  1.更新時のロック機能(レイヤ)を追加

2014/03/13  1.更新時のロック機能(レイヤ)の修正漏れ

2014/03/28  1.更新時のロック機能(レイヤ)修正

2014/04/25  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効

2016/01/13  1.コールしたプログラムがKNJD137Lのとき、学期コンボは9学期のみ表示に変更

2018/09/04  1.コールしたプログラムがKNJD137Kの時、学期コンボは「DP78」を参照

2018/09/10  1.コールしたプログラムがKNJD137Lのとき、学期コンボは「'9学期以外で最大の学期'以外」を表示に変更

2019/04/19  1.更新後、右画面のロックが解除されない不具合修正

2020/02/20 01.PC-Talkerの機能

2020/06/15 1.プロパティ「knjdBehaviorsd_DispViewName」を追加
           --1の場合 ：項目名にBEHAVIOR_SEMES_MSTのVIEWNAMEフィールドを参照
           --それ以外：項目名に名称マスタ「D035」のNAME1フィールドを参照(デフォルト)
