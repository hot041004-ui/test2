export interface ScienceQuestion {
  id: number;
  subject: '물리' | '화학' | '생물' | '지구과학';
  grade: string;
  title: string;
  question: string;
  options: [string, string, string, string];
  answer: 1 | 2 | 3 | 4;
  explanation: string;
}

export interface UserAnswerRecord {
  selected: number;
  correct: number;
  isCorrect: boolean;
}

export interface HandLandmarkPoint {
  x: number;
  y: number;
  z: number;
}
